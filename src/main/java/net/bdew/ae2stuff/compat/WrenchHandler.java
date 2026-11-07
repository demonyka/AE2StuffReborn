package net.bdew.ae2stuff.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface WrenchHandler {
    boolean canWrench(PlayerEntity player, ItemStack stack, BlockPos pos);

    void doWrench(PlayerEntity player, ItemStack stack, BlockPos pos);
}
