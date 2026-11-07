package net.bdew.ae2stuff.items;

import appeng.api.config.SecurityPermissions;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.grid.Security;
import net.bdew.ae2stuff.machines.wireless.BlockWireless;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.bdew.ae2stuff.misc.ItemLocationStore;
import net.bdew.ae2stuff.misc.PosAndDimension;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWirelessKit extends ItemLocationStore {
    public ItemWirelessKit() {
        super(new Properties().group(Registration.TAB).maxStackSize(1));
    }

    private static void status(PlayerEntity player, TextFormatting color, String key, Object... args) {
        player.sendStatusMessage(new TranslationTextComponent(key, args).mergeStyle(color), true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !world.isRemote) {
            ItemStack copy = stack.copy();
            clearLocation(copy);
            return ActionResult.resultSuccess(copy);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        if (player == null || !(world.getBlockState(pos).getBlock() instanceof BlockWireless))
            return ActionResultType.PASS;

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileWireless)
                useOnTile((TileWireless) tile, context.getItem(), player, world, pos);
        }

        return ActionResultType.SUCCESS;
    }

    private void useOnTile(TileWireless tile, ItemStack stack, PlayerEntity player, World world, BlockPos pos) {
        int pid = Security.getPlayerId(player);

        // Check that the player can modify the network
        if (!Security.playerHasPermission(tile.getNode().getGrid(), pid, SecurityPermissions.BUILD)) {
            status(player, TextFormatting.RED, "ae2stuff.wireless.tool.security.player");
            return;
        }

        PosAndDimension otherLoc = getLocation(stack);

        if (otherLoc == null) {
            // Have no location stored - store current location
            status(player, TextFormatting.GREEN, "ae2stuff.wireless.tool.bound1", pos.getX(), pos.getY(), pos.getZ());
            setLocation(stack, pos, world);
        } else if (!otherLoc.dim.equals(PosAndDimension.dimensionOf(world))) {
            // Different dimensions - error out
            status(player, TextFormatting.RED, "ae2stuff.wireless.tool.dimension");
            clearLocation(stack);
        } else if (pos.equals(otherLoc.pos)) {
            // Same block - clear the location
            clearLocation(stack);
        } else {
            TileEntity otherTile = world.getTileEntity(otherLoc.pos);
            if (otherTile instanceof TileWireless) {
                // The other tile is still around
                TileWireless other = (TileWireless) otherTile;
                // Check that the player can modify it too
                if (!Security.playerHasPermission(other.getNode().getGrid(), pid, SecurityPermissions.BUILD)) {
                    status(player, TextFormatting.RED, "ae2stuff.wireless.tool.security.player");
                } else {
                    // Player can modify both sides - unlink current connections if any
                    tile.doUnlink();
                    other.doUnlink();
                    // Make player the owner of both blocks
                    tile.getNode().setPlayerID(pid);
                    other.getNode().setPlayerID(pid);
                    if (tile.doLink(other))
                        status(player, TextFormatting.GREEN, "ae2stuff.wireless.tool.connected", pos.getX(), pos.getY(), pos.getZ());
                    else
                        status(player, TextFormatting.RED, "ae2stuff.wireless.tool.failed");
                }
            } else {
                // The other block is gone - error out
                status(player, TextFormatting.RED, "ae2stuff.wireless.tool.noexist");
            }
            clearLocation(stack);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        PosAndDimension loc = getLocation(stack);
        if (loc != null) {
            tooltip.add(new TranslationTextComponent("ae2stuff.wireless.tool.bound1", loc.pos.getX(), loc.pos.getY(), loc.pos.getZ()));
            tooltip.add(new TranslationTextComponent("ae2stuff.wireless.tool.bound2"));
        } else {
            tooltip.add(new TranslationTextComponent("ae2stuff.wireless.tool.empty"));
        }
    }
}
