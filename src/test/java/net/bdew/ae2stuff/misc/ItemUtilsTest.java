package net.bdew.ae2stuff.misc;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemUtilsTest {
    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    /**
     * Inventory with a configurable stack limit that can refuse some slots.
     */
    private static final class TestInventory extends Inventory {
        private final int limit;
        private final int refusedSlot;
        int dirtyCount;

        TestInventory(int size, int limit, int refusedSlot) {
            super(size);
            this.limit = limit;
            this.refusedSlot = refusedSlot;
        }

        TestInventory(int size) {
            this(size, 64, -1);
        }

        @Override
        public int getInventoryStackLimit() {
            return limit;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return slot != refusedSlot;
        }

        @Override
        public void markDirty() {
            dirtyCount++;
            super.markDirty();
        }
    }

    private static ItemStack named(Item item, int count, String name) {
        ItemStack stack = new ItemStack(item, count);
        stack.getOrCreateTag().putString("name", name);
        return stack;
    }

    private static int count(TestInventory inv, Item item) {
        int total = 0;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.getItem() == item)
                total += stack.getCount();
        }
        return total;
    }

    @Test
    void mergesIntoExistingStacksBeforeUsingEmptySlots() {
        TestInventory inv = new TestInventory(3);
        inv.setInventorySlotContents(1, new ItemStack(Items.STONE, 60));
        inv.setInventorySlotContents(2, new ItemStack(Items.STONE, 10));

        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.STONE, 20), inv, 0, 3, false);

        assertTrue(left.isEmpty());
        assertTrue(inv.getStackInSlot(0).isEmpty(), "the empty slot must stay empty while existing stacks have room");
        assertEquals(64, inv.getStackInSlot(1).getCount());
        assertEquals(26, inv.getStackInSlot(2).getCount());
        assertTrue(inv.dirtyCount > 0);
    }

    @Test
    void respectsItemMaxStackSize() {
        TestInventory inv = new TestInventory(2);
        inv.setInventorySlotContents(0, new ItemStack(Items.ENDER_PEARL, 10));
        inv.setInventorySlotContents(1, new ItemStack(Items.ENDER_PEARL, 15));

        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.ENDER_PEARL, 16), inv, 0, 2, false);

        assertEquals(16, inv.getStackInSlot(0).getCount());
        assertEquals(16, inv.getStackInSlot(1).getCount());
        assertEquals(9, left.getCount());
        assertEquals(Items.ENDER_PEARL, left.getItem());
    }

    @Test
    void respectsInventoryStackLimitOfOne() {
        TestInventory inv = new TestInventory(3, 1, -1);
        inv.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 1));

        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.DIAMOND, 5), inv, 0, 3, false);

        for (int slot = 0; slot < 3; slot++)
            assertEquals(1, inv.getStackInSlot(slot).getCount(), "slot " + slot);
        assertEquals(3, left.getCount());
    }

    @Test
    void returnsLeftoversWhenFull() {
        TestInventory inv = new TestInventory(2);
        inv.setInventorySlotContents(0, new ItemStack(Items.STONE, 60));
        inv.setInventorySlotContents(1, new ItemStack(Items.DIRT, 1));

        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.STONE, 10), inv, 0, 2, false);

        assertEquals(64, inv.getStackInSlot(0).getCount());
        assertEquals(Items.DIRT, inv.getStackInSlot(1).getItem());
        assertEquals(1, inv.getStackInSlot(1).getCount());
        assertEquals(Items.STONE, left.getItem());
        assertEquals(6, left.getCount());
    }

    @Test
    void checkValidSkipsRefusedSlots() {
        TestInventory checked = new TestInventory(2, 64, 0);
        checked.setInventorySlotContents(0, new ItemStack(Items.STONE, 1));
        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.STONE, 70), checked, 0, 2, true);
        assertEquals(1, checked.getStackInSlot(0).getCount(), "refused slot must not be merged into");
        assertEquals(64, checked.getStackInSlot(1).getCount());
        assertEquals(6, left.getCount());

        TestInventory unchecked = new TestInventory(2, 64, 0);
        left = ItemUtils.addStackToSlots(new ItemStack(Items.STONE, 70), unchecked, 0, 2, false);
        assertEquals(64, unchecked.getStackInSlot(0).getCount());
        assertEquals(6, unchecked.getStackInSlot(1).getCount());
        assertTrue(left.isEmpty());
    }

    @Test
    void staysWithinSlotRange() {
        TestInventory inv = new TestInventory(4);
        inv.setInventorySlotContents(0, new ItemStack(Items.STONE, 1));

        ItemStack left = ItemUtils.addStackToSlots(new ItemStack(Items.STONE, 100), inv, 1, 3, false);

        assertEquals(1, inv.getStackInSlot(0).getCount());
        assertEquals(64, inv.getStackInSlot(1).getCount());
        assertEquals(36, inv.getStackInSlot(2).getCount());
        assertTrue(inv.getStackInSlot(3).isEmpty());
        assertTrue(left.isEmpty());
    }

    @Test
    void doesNotMergeDifferentTags() {
        TestInventory inv = new TestInventory(2);
        inv.setInventorySlotContents(0, named(Items.STONE, 1, "a"));

        ItemStack left = ItemUtils.addStackToSlots(named(Items.STONE, 1, "b"), inv, 0, 2, false);

        assertTrue(left.isEmpty());
        assertEquals(1, inv.getStackInSlot(0).getCount());
        assertEquals("a", inv.getStackInSlot(0).getTag().getString("name"));
        assertEquals("b", inv.getStackInSlot(1).getTag().getString("name"));
    }

    @Test
    void insertedStacksAreNotSharedWithTheCaller() {
        TestInventory inv = new TestInventory(1);
        ItemStack stack = new ItemStack(Items.STONE, 5);

        ItemUtils.addStackToSlots(stack, inv, 0, 1, false);
        stack.setCount(40);

        assertEquals(5, inv.getStackInSlot(0).getCount());
    }

    @Test
    void emptyStackIsReturnedUnchanged() {
        TestInventory inv = new TestInventory(1);
        assertSame(ItemStack.EMPTY, ItemUtils.addStackToSlots(ItemStack.EMPTY, inv, 0, 1, false));
        assertTrue(inv.getStackInSlot(0).isEmpty());
    }

    @Test
    void neverDuplicatesOrLosesItems() {
        Item[] items = {Items.STONE, Items.ENDER_PEARL, Items.DIAMOND};
        Random random = new Random(42);
        for (int round = 0; round < 500; round++) {
            int size = 1 + random.nextInt(6);
            int limit = 1 + random.nextInt(64);
            TestInventory inv = new TestInventory(size, limit, random.nextBoolean() ? random.nextInt(size) : -1);
            for (int slot = 0; slot < size; slot++) {
                if (random.nextBoolean()) {
                    Item item = items[random.nextInt(items.length)];
                    inv.setInventorySlotContents(slot, new ItemStack(item, 1 + random.nextInt(Math.min(limit, item.getMaxStackSize()))));
                }
            }
            Item item = items[random.nextInt(items.length)];
            int added = 1 + random.nextInt(item.getMaxStackSize());
            int before = count(inv, item);
            int[] otherBefore = new int[items.length];
            for (int i = 0; i < items.length; i++)
                otherBefore[i] = count(inv, items[i]);
            int from = random.nextInt(size);
            int to = from + 1 + random.nextInt(size - from);
            boolean checkValid = random.nextBoolean();

            ItemStack left = ItemUtils.addStackToSlots(new ItemStack(item, added), inv, from, to, checkValid);

            String context = "round " + round;
            assertTrue(left.isEmpty() || left.getItem() == item, context);
            assertEquals(before + added, count(inv, item) + left.getCount(), context);
            for (int i = 0; i < items.length; i++) {
                if (items[i] != item)
                    assertEquals(otherBefore[i], count(inv, items[i]), context);
            }
            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = inv.getStackInSlot(slot);
                assertTrue(stack.getCount() <= stack.getMaxStackSize(), context + " slot " + slot);
                assertTrue(stack.getCount() <= limit, context + " slot " + slot);
            }
        }
    }

    @Test
    void isSameItemComparesItemAndTagButNotCount() {
        assertTrue(ItemUtils.isSameItem(ItemStack.EMPTY, ItemStack.EMPTY));
        assertFalse(ItemUtils.isSameItem(ItemStack.EMPTY, new ItemStack(Items.STONE)));
        assertFalse(ItemUtils.isSameItem(new ItemStack(Items.STONE), ItemStack.EMPTY));
        assertTrue(ItemUtils.isSameItem(new ItemStack(Items.STONE, 1), new ItemStack(Items.STONE, 64)));
        assertFalse(ItemUtils.isSameItem(new ItemStack(Items.STONE), new ItemStack(Items.DIRT)));
        assertTrue(ItemUtils.isSameItem(named(Items.STONE, 1, "a"), named(Items.STONE, 3, "a")));
        assertFalse(ItemUtils.isSameItem(named(Items.STONE, 1, "a"), named(Items.STONE, 1, "b")));
        assertFalse(ItemUtils.isSameItem(named(Items.STONE, 1, "a"), new ItemStack(Items.STONE)));
    }
}
