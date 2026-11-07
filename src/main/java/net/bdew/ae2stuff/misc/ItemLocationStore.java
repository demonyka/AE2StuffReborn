package net.bdew.ae2stuff.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public abstract class ItemLocationStore extends Item {
    private static final String TAG = "loc";

    protected ItemLocationStore(Properties properties) {
        super(properties);
    }

    @Nullable
    public PosAndDimension getLocation(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains(TAG, Constants.NBT.TAG_COMPOUND))
            return PosAndDimension.decode(tag.getCompound(TAG));
        return null;
    }

    public void setLocation(ItemStack stack, BlockPos pos, World world) {
        stack.getOrCreateTag().put(TAG, new PosAndDimension(pos, PosAndDimension.dimensionOf(world)).encode());
    }

    public void clearLocation(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null)
            tag.remove(TAG);
    }
}
