package net.bdew.ae2stuff.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Exposes an item handler as an inventory, so container slots can use it.
 */
public class InventoryHandlerAdapter implements IInventory {
    private final IItemHandler handler;

    public InventoryHandlerAdapter(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < handler.getSlots(); i++)
            removeStackFromSlot(i);
    }

    @Override
    public int getInventoryStackLimit() {
        int limit = 0;
        for (int i = 0; i < handler.getSlots(); i++)
            limit = Math.max(limit, handler.getSlotLimit(i));
        return limit;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return handler.insertItem(index, stack, true).getCount() < stack.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return handler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return handler.extractItem(index, handler.getStackInSlot(index).getCount(), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (handler instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) handler).setStackInSlot(index, stack);
        } else {
            removeStackFromSlot(index);
            handler.insertItem(index, stack, false);
        }
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }
}
