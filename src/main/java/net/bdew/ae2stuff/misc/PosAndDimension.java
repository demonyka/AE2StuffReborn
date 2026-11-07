package net.bdew.ae2stuff.misc;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A position in a dimension. Dimensions are identified by name since 1.16, the numeric ids are gone.
 */
public final class PosAndDimension {
    public final BlockPos pos;
    public final ResourceLocation dim;

    public PosAndDimension(BlockPos pos, ResourceLocation dim) {
        this.pos = pos;
        this.dim = dim;
    }

    public static ResourceLocation dimensionOf(World world) {
        return world.getDimensionKey().getLocation();
    }

    public CompoundNBT encode() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("dim", dim.toString());
        return tag;
    }

    @Nullable
    public static PosAndDimension decode(CompoundNBT tag) {
        if (tag.contains("x", Constants.NBT.TAG_INT) && tag.contains("y", Constants.NBT.TAG_INT)
                && tag.contains("z", Constants.NBT.TAG_INT) && tag.contains("dim", Constants.NBT.TAG_STRING)) {
            return new PosAndDimension(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")), new ResourceLocation(tag.getString("dim")));
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PosAndDimension))
            return false;
        PosAndDimension that = (PosAndDimension) other;
        return pos.equals(that.pos) && dim.equals(that.dim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, dim);
    }
}
