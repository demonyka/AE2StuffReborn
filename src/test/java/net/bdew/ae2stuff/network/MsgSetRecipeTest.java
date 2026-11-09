package net.bdew.ae2stuff.network;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.bdew.ae2stuff.machines.encoder.TileEncoder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MsgSetRecipeTest {
    // Client to server packets above 32767 bytes disconnect the player
    private static final int CLIENT_PACKET_LIMIT = 32767;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    private static byte[] encode(MsgSetRecipe message) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        try {
            message.encode(buf);
            return ByteBufUtil.getBytes(buf);
        } finally {
            buf.release();
        }
    }

    /**
     * Reads the wire format back into plain lists, independent of the message class.
     */
    private static List<List<ItemStack>> parse(byte[] bytes) {
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(bytes));
        List<List<ItemStack>> slots = new ArrayList<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            int options = buf.readVarInt();
            List<ItemStack> slot = new ArrayList<>();
            for (int j = 0; j < options; j++)
                slot.add(buf.readItemStack());
            slots.add(slot);
        }
        assertEquals(0, buf.readableBytes());
        return slots;
    }

    private static ItemStack tagged(Item item, int id, int padding) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt("id", id);
        if (padding > 0)
            stack.getOrCreateTag().putString("padding", String.join("", Collections.nCopies(padding, "x")));
        return stack;
    }

    private static List<ItemStack> options(int count, int padding) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < count; i++)
            list.add(tagged(Items.STONE, i, padding));
        return list;
    }

    private static void assertSameStacks(List<ItemStack> expected, List<ItemStack> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++)
            assertTrue(ItemStack.areItemStacksEqual(expected.get(i), actual.get(i)), "stack " + i);
    }

    @Test
    void encodeDecodeRoundTrip() {
        List<List<ItemStack>> recipe = Arrays.asList(
                Arrays.asList(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BIRCH_PLANKS)),
                Collections.emptyList(),
                Collections.singletonList(tagged(Items.DIAMOND, 7, 10)));
        byte[] bytes = encode(MsgSetRecipe.fit(recipe));

        List<List<ItemStack>> parsed = parse(bytes);
        assertEquals(recipe.size(), parsed.size());
        for (int i = 0; i < recipe.size(); i++)
            assertSameStacks(recipe.get(i), parsed.get(i));

        MsgSetRecipe decoded = MsgSetRecipe.decode(new PacketBuffer(Unpooled.wrappedBuffer(bytes)));
        assertArrayEquals(bytes, encode(decoded));
    }

    @Test
    void fitLimitsSlotsAndOptions() {
        List<List<ItemStack>> recipe = new ArrayList<>();
        for (int i = 0; i < TileEncoder.SLOT_RECIPE_COUNT + 3; i++)
            recipe.add(Collections.singletonList(new ItemStack(Items.STONE)));
        recipe.set(0, options(300, 0));

        List<List<ItemStack>> parsed = parse(encode(MsgSetRecipe.fit(recipe)));

        assertEquals(TileEncoder.SLOT_RECIPE_COUNT, parsed.size());
        assertSameStacks(recipe.get(0).subList(0, 256), parsed.get(0));
    }

    @Test
    void fitShrinksHugeRecipesIntoOnePacketKeepingTheFirstOptions() {
        List<List<ItemStack>> recipe = new ArrayList<>();
        for (int i = 0; i < TileEncoder.SLOT_RECIPE_COUNT; i++)
            recipe.add(i == 4 ? Collections.singletonList(tagged(Items.DIAMOND, 0, 2000)) : options(256, 200));

        byte[] bytes = encode(MsgSetRecipe.fit(recipe));
        assertTrue(bytes.length < CLIENT_PACKET_LIMIT - 1000, "payload is " + bytes.length + " bytes");

        List<List<ItemStack>> parsed = parse(bytes);
        assertEquals(recipe.size(), parsed.size());
        for (int i = 0; i < recipe.size(); i++) {
            List<ItemStack> slot = parsed.get(i);
            assertTrue(!slot.isEmpty(), "slot " + i + " lost all its options");
            assertSameStacks(recipe.get(i).subList(0, slot.size()), slot);
        }
        assertEquals(1, parsed.get(4).size());
    }

    @Test
    void fitKeepsOneOptionPerSlotEvenIfThatIsTooBig() {
        List<List<ItemStack>> recipe = new ArrayList<>();
        for (int i = 0; i < TileEncoder.SLOT_RECIPE_COUNT; i++)
            recipe.add(Arrays.asList(tagged(Items.STONE, i, 5000), tagged(Items.DIRT, i, 5000)));

        List<List<ItemStack>> parsed = parse(encode(MsgSetRecipe.fit(recipe)));

        for (int i = 0; i < recipe.size(); i++)
            assertSameStacks(recipe.get(i).subList(0, 1), parsed.get(i));
    }

    private static PacketBuffer header(int slots, int... options) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(slots);
        for (int count : options)
            buf.writeVarInt(count);
        return buf;
    }

    @Test
    void decodeRejectsBadCounts() {
        assertThrows(DecoderException.class, () -> MsgSetRecipe.decode(header(TileEncoder.SLOT_RECIPE_COUNT + 1)));
        assertThrows(DecoderException.class, () -> MsgSetRecipe.decode(header(-1)));
        assertThrows(DecoderException.class, () -> MsgSetRecipe.decode(header(1, 257)));
        assertThrows(DecoderException.class, () -> MsgSetRecipe.decode(header(1, -1)));
    }

    @Test
    void decodeAcceptsLimits() {
        PacketBuffer buf = header(TileEncoder.SLOT_RECIPE_COUNT);
        for (int i = 0; i < TileEncoder.SLOT_RECIPE_COUNT; i++) {
            buf.writeVarInt(i == 0 ? 256 : 0);
            if (i == 0) {
                for (int j = 0; j < 256; j++)
                    buf.writeItemStack(new ItemStack(Items.STONE), false);
            }
        }
        byte[] input = ByteBufUtil.getBytes(buf);
        assertArrayEquals(input, encode(MsgSetRecipe.decode(new PacketBuffer(Unpooled.wrappedBuffer(input)))));
    }
}
