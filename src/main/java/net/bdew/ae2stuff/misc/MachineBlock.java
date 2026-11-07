package net.bdew.ae2stuff.misc;

import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.util.Platform;
import net.bdew.ae2stuff.compat.WrenchHandler;
import net.bdew.ae2stuff.compat.WrenchRegistry;
import net.bdew.ae2stuff.grid.GridTile;
import net.bdew.ae2stuff.grid.InventoryGridTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Common behaviour of all machines: active texture, wrench handling and keeping the tile data when broken.
 */
public abstract class MachineBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final Material MATERIAL = new Material.Builder(MaterialColor.IRON).build();

    protected MachineBlock() {
        super(AbstractBlock.Properties.create(MATERIAL).hardnessAndResistance(1F));
        setDefaultState(getStateContainer().getBaseState().with(ACTIVE, false));
    }

    public static void setActive(World world, BlockPos pos, boolean active) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof MachineBlock && state.get(ACTIVE) != active)
            world.setBlockState(pos, state.with(ACTIVE, active), 3);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    /**
     * Called on the server when the block is used normally.
     *
     * @return false to let the held item act on the block
     */
    protected abstract boolean onBlockActivatedReal(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit);

    /**
     * Rotates the block to face a side. AE2 wrenches only do this by themselves for AE2 blocks.
     *
     * @return true if this block can be rotated
     */
    protected boolean rotateToSide(BlockState state, World world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (player.isSneaking()) {
            ItemStack stack = player.inventory.getCurrentItem();
            WrenchHandler wrench = WrenchRegistry.findWrench(player, stack, pos);
            if (wrench != null) {
                wrench.doWrench(player, stack, pos);
                if (!world.isRemote)
                    world.destroyBlock(pos, true);
                return ActionResultType.func_233537_a_(world.isRemote);
            }
            return ActionResultType.PASS;
        }

        if (world.isRemote)
            return ActionResultType.SUCCESS;

        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof IAEWrench
                && Platform.hasPermissions(new DimensionalCoord(world, pos), player)
                && rotateToSide(state, world, pos, hit.getFace())) {
            player.swingArm(hand);
            return ActionResultType.CONSUME;
        }

        return onBlockActivatedReal(state, world, pos, player, hand, hit) ? ActionResultType.CONSUME : ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof GridTile && placer instanceof PlayerEntity)
            ((GridTile) tile).placingPlayer = (PlayerEntity) placer;
        if (tile instanceof InventoryGridTile && !world.isRemote)
            ((InventoryGridTile) tile).loadFromItem(stack);
    }

    private ItemStack getSavedBlock(@Nullable TileEntity tile) {
        ItemStack stack = new ItemStack(this);
        if (tile instanceof InventoryGridTile)
            ((InventoryGridTile) tile).saveToItem(stack);
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        Float explosionRadius = builder.get(LootParameters.EXPLOSION_RADIUS);
        if (explosionRadius != null && builder.getWorld().rand.nextFloat() > 1F / explosionRadius)
            return Collections.emptyList();
        return Collections.singletonList(getSavedBlock(builder.get(LootParameters.BLOCK_ENTITY)));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getSavedBlock(world.getTileEntity(pos));
    }

    protected final void openContainer(PlayerEntity player, BlockPos pos, IContainerProvider provider) {
        // Fake players of other mods have no connection to open a screen on
        if (player instanceof FakePlayer)
            return;
        TranslationTextComponent title = new TranslationTextComponent(getTranslationKey());
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(provider, title), pos);
    }
}
