package net.bdew.ae2stuff.compat;

import appeng.api.implementations.items.IAEWrench;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class AEWrenchHandler implements WrenchHandler {
    @Override
    public boolean canWrench(PlayerEntity player, ItemStack stack, BlockPos pos) {
        return stack.getItem() instanceof IAEWrench && ((IAEWrench) stack.getItem()).canWrench(stack, player, pos);
    }

    @Override
    public void doWrench(PlayerEntity player, ItemStack stack, BlockPos pos) {
    }
}
