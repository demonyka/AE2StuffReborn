package net.bdew.ae2stuff.misc;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PosAndDimensionTest {
    private static final ResourceLocation NETHER = new ResourceLocation("minecraft", "the_nether");
    private static final ResourceLocation MODDED = new ResourceLocation("somemod", "deep/space");

    @Test
    void encodeDecodeRoundTrip() {
        PosAndDimension original = new PosAndDimension(new BlockPos(-30000000, -64, 29999999), MODDED);
        PosAndDimension decoded = PosAndDimension.decode(original.encode());
        assertNotNull(decoded);
        assertEquals(original.pos, decoded.pos);
        assertEquals(original.dim, decoded.dim);
    }

    @Test
    void decodeOfIncompleteTagIsNull() {
        CompoundNBT full = new PosAndDimension(new BlockPos(1, 2, 3), NETHER).encode();
        for (String key : new String[]{"x", "y", "z", "dim"}) {
            CompoundNBT tag = full.copy();
            tag.remove(key);
            assertNull(PosAndDimension.decode(tag), "tag without " + key);
        }
        assertNull(PosAndDimension.decode(new CompoundNBT()));
    }

    @Test
    void decodeOfWrongTypesIsNull() {
        CompoundNBT tag = new PosAndDimension(new BlockPos(1, 2, 3), NETHER).encode();
        tag.putString("x", "1");
        assertNull(PosAndDimension.decode(tag));

        // Pre-1.16 bindings stored the dimension as a number
        CompoundNBT old = new PosAndDimension(new BlockPos(1, 2, 3), NETHER).encode();
        old.putInt("dim", -1);
        assertNull(PosAndDimension.decode(old));
    }

    @Test
    void equalsAndHashCode() {
        PosAndDimension a = new PosAndDimension(new BlockPos(1, 2, 3), NETHER);
        PosAndDimension same = new PosAndDimension(new BlockPos(1, 2, 3), new ResourceLocation("the_nether"));
        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());

        assertNotEquals(a, new PosAndDimension(new BlockPos(1, 2, 4), NETHER));
        assertNotEquals(a, new PosAndDimension(new BlockPos(1, 2, 3), MODDED));
        assertNotEquals(a, null);
        assertNotEquals(a, new BlockPos(1, 2, 3));
    }
}
