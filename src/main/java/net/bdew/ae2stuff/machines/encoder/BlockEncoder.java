package net.bdew.ae2stuff.machines.encoder;

import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockEncoder extends MachineBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockEncoder() {
        setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEncoder();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null)
            return getDefaultState();
        return getDefaultState().with(FACING, getFacingFromPlayer(player));
    }

    private static Direction getFacingFromPlayer(PlayerEntity player) {
        int pitch = Math.round(player.rotationPitch);
        if (pitch >= 50)
            return Direction.UP;
        if (pitch <= -50)
            return Direction.DOWN;
        switch (MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0:
                return Direction.NORTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.SOUTH;
            default:
                return Direction.WEST;
        }
    }

    @Override
    protected boolean rotateToSide(BlockState state, World world, BlockPos pos, Direction side) {
        world.setBlockState(pos, state.with(FACING, side), 3);
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(FACING)));
    }

    @Override
    protected boolean onBlockActivatedReal(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEncoder && ((TileEncoder) tile).isOnline()) {
            TileEncoder encoder = (TileEncoder) tile;
            openContainer(player, pos, (id, inventory, p) -> new ContainerEncoder(id, inventory, encoder));
        } else {
            player.sendStatusMessage(new TranslationTextComponent("ae2stuff.error.not_connected").mergeStyle(TextFormatting.RED), true);
        }
        return true;
    }
}
