package net.bdew.ae2stuff.machines.grower;

import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockGrower extends MachineBlock {
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileGrower();
    }

    @Override
    protected boolean onBlockActivatedReal(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGrower)
            openContainer(player, pos, (id, inventory, p) -> new ContainerGrower(id, inventory, (TileGrower) tile));
        return true;
    }
}
