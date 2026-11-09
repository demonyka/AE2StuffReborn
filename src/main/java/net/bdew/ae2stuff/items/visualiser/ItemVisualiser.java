package net.bdew.ae2stuff.items.visualiser;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VLink;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VNode;
import net.bdew.ae2stuff.misc.ItemLocationStore;
import net.bdew.ae2stuff.misc.PosAndDimension;
import net.bdew.ae2stuff.network.MsgVisualisationData;
import net.bdew.ae2stuff.network.NetHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemVisualiser extends ItemLocationStore {
    public ItemVisualiser() {
        super(new Properties().group(Registration.TAB).maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        if (!(world.getTileEntity(pos) instanceof IGridHost))
            return ActionResultType.PASS;
        if (!world.isRemote && player != null) {
            setLocation(context.getItem(), pos, world);
            player.sendStatusMessage(new TranslationTextComponent("ae2stuff.visualiser.bound", pos.getX(), pos.getY(), pos.getZ()).mergeStyle(TextFormatting.GREEN), true);
        }
        return ActionResultType.SUCCESS;
    }

    public VisualisationModes getMode(ItemStack stack) {
        return stack.hasTag() ? VisualisationModes.byId(stack.getTag().getByte("mode")) : VisualisationModes.FULL;
    }

    public void setMode(ItemStack stack, VisualisationModes mode) {
        stack.getOrCreateTag().putByte("mode", (byte) mode.ordinal());
    }

    public void handleModeChange(ServerPlayerEntity player, VisualisationModes mode) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack.getItem() == this) {
            setMode(stack, mode);
            ITextComponent modeName = new TranslationTextComponent(mode.translationKey()).mergeStyle(TextFormatting.YELLOW);
            player.sendStatusMessage(new TranslationTextComponent("ae2stuff.visualiser.set", modeName), true);
        }
    }

    /**
     * @return the visualiser held by the player, or an empty stack
     */
    public ItemStack getHeldStack(PlayerEntity player) {
        ItemStack main = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        if (main.getItem() == this)
            return main;
        ItemStack off = player.getItemStackFromSlot(EquipmentSlotType.OFFHAND);
        return off.getItem() == this ? off : ItemStack.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack unused, World world, Entity entity, int slot, boolean active) {
        if (world.isRemote || !(entity instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        ItemStack stack = getHeldStack(player);
        if (stack.isEmpty())
            return;

        PosAndDimension boundLoc = getLocation(stack);
        IGrid grid = findGrid(boundLoc, world);
        if (grid == null) {
            if (VisualiserPlayerTracker.needToClear(player))
                NetHandler.sendTo(new MsgVisualisationData(new VisualisationData()), player);
            return;
        }
        if (VisualiserPlayerTracker.needToUpdate(player, boundLoc))
            NetHandler.sendTo(new MsgVisualisationData(buildData(grid, world)), player);
    }

    @Nullable
    private static IGrid findGrid(@Nullable PosAndDimension boundLoc, World world) {
        // A bound network in an unloaded chunk is not loaded just to be drawn
        if (boundLoc == null || !boundLoc.dim.equals(PosAndDimension.dimensionOf(world)) || !world.isBlockLoaded(boundLoc.pos))
            return null;
        TileEntity tile = world.getTileEntity(boundLoc.pos);
        if (!(tile instanceof IGridHost))
            return null;
        IGridNode hostNode = ((IGridHost) tile).getGridNode(AEPartLocation.INTERNAL);
        return hostNode != null ? hostNode.getGrid() : null;
    }

    private static VisualisationData buildData(IGrid grid, World world) {
        Set<IGridConnection> connections = new HashSet<>();
        Map<IGridNode, VNode> nodes = new HashMap<>();
        // Parts and their cable share a position, each distinct node is sent once
        Map<VNode, VNode> unique = new LinkedHashMap<>();

        for (IGridNode node : grid.getNodes()) {
            IGridBlock block = node.getGridBlock();
            if (block.isWorldAccessible() && block.getLocation().isInWorld(world)) {
                DimensionalCoord loc = block.getLocation();
                int flags = 0;
                if (!node.meetsChannelRequirements())
                    flags |= VisualisationData.NODE_MISSING;
                if (node.hasFlag(GridFlags.DENSE_CAPACITY))
                    flags |= VisualisationData.NODE_DENSE;
                VNode vnode = new VNode(loc.x, loc.y, loc.z, flags);
                VNode existing = unique.get(vnode);
                if (existing == null) {
                    // A huge network is shown partially rather than overflowing the packet
                    if (unique.size() >= VisualisationData.MAX_NODES)
                        continue;
                    unique.put(vnode, vnode);
                    existing = vnode;
                }
                nodes.put(node, existing);
                node.getConnections().forEach(connections::add);
            }
        }

        List<VLink> links = new ArrayList<>();
        for (IGridConnection connection : connections) {
            if (links.size() >= VisualisationData.MAX_LINKS)
                break;
            VNode node1 = nodes.get(connection.a());
            VNode node2 = nodes.get(connection.b());
            if (node1 != null && node2 != null && !node1.equals(node2)) {
                int flags = 0;
                if (connection.a().hasFlag(GridFlags.DENSE_CAPACITY) && connection.b().hasFlag(GridFlags.DENSE_CAPACITY))
                    flags |= VisualisationData.LINK_DENSE;
                if (connection.a().hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED) && connection.b().hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED))
                    flags |= VisualisationData.LINK_COMPRESSED;
                links.add(new VLink(node1, node2, connection.getUsedChannels(), flags));
            }
        }

        return new VisualisationData(new ArrayList<>(unique.values()), links);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(new TranslationTextComponent("ae2stuff.visualiser.mode").appendString(" ")
                .appendSibling(new TranslationTextComponent(getMode(stack).translationKey()).mergeStyle(TextFormatting.YELLOW)));
    }
}
