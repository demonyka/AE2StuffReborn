package net.bdew.ae2stuff.machines.wireless;

import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockWireless extends MachineBlock {
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileWireless();
    }

    @Override
    protected boolean onBlockActivatedReal(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileWireless)
                ((TileWireless) tile).doUnlink();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }
}
