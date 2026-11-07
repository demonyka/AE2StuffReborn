package net.bdew.ae2stuff.misc;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public final class ItemUtils {
    private ItemUtils() {
    }

    public static void throwItemAt(World world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty() || world.isRemote)
            return;
        double dx = world.rand.nextFloat() * 0.8D;
        double dy = world.rand.nextFloat() * 0.8D;
        double dz = world.rand.nextFloat() * 0.8D;
        ItemEntity entity = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);
        entity.setMotion(world.rand.nextGaussian() * 0.05D, world.rand.nextGaussian() * 0.05D + 0.2D, world.rand.nextGaussian() * 0.05D);
        world.addEntity(entity);
    }

    public static boolean isSameItem(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty())
            return stack1.isEmpty() == stack2.isEmpty();
        return stack1.getItem() == stack2.getItem() && ItemStack.areItemStackTagsEqual(stack2, stack1);
    }

    /**
     * Adds a stack to the slots in the range [from, to), merging into existing stacks first.
     *
     * @return what didn't fit
     */
    public static ItemStack addStackToSlots(ItemStack stack, IInventory inv, int from, int to, boolean checkValid) {
        if (stack.isEmpty())
            return stack;

        // Try merging into existing slots
        for (int slot = from; slot < to; slot++) {
            if ((!checkValid || inv.isItemValidForSlot(slot, stack)) && isSameItem(stack, inv.getStackInSlot(slot))) {
                ItemStack target = inv.getStackInSlot(slot);
                int space = Math.min(target.getMaxStackSize(), inv.getInventoryStackLimit()) - target.getCount();
                int toAdd = Math.min(space, stack.getCount());
                if (toAdd >= stack.getCount()) {
                    target.grow(stack.getCount());
                    stack.setCount(0);
                    inv.markDirty();
                    return ItemStack.EMPTY;
                } else if (toAdd > 0) {
                    target.grow(toAdd);
                    stack.shrink(toAdd);
                    inv.markDirty();
                }
            }
        }

        // Now find empty slots and stick any leftovers there
        for (int slot = from; slot < to; slot++) {
            if ((!checkValid || inv.isItemValidForSlot(slot, stack)) && inv.getStackInSlot(slot).isEmpty()) {
                if (inv.getInventoryStackLimit() < stack.getCount()) {
                    inv.setInventorySlotContents(slot, stack.split(inv.getInventoryStackLimit()));
                } else {
                    inv.setInventorySlotContents(slot, stack.copy());
                    stack.setCount(0);
                    return ItemStack.EMPTY;
                }
            }
        }

        return stack;
    }

    public static ItemStack addStackToHandler(ItemStack stack, IItemHandler inv) {
        if (stack.isEmpty())
            return stack;

        ItemStack left = stack.copy();

        // Try merging into existing slots
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            if (isSameItem(stack, inv.getStackInSlot(slot))) {
                left = inv.insertItem(slot, left, false);
                if (left.isEmpty())
                    return left;
            }
        }

        // Now find empty slots and stick any leftovers there
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            if (inv.getStackInSlot(slot).isEmpty()) {
                left = inv.insertItem(slot, left, false);
                if (left.isEmpty())
                    return left;
            }
        }

        return left;
    }

    /**
     * @return the first slot with the item, or -1
     */
    public static int findItemInInventory(IInventory inv, Item item) {
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == item)
                return slot;
        }
        return -1;
    }
}
