package net.bdew.ae2stuff.machines.encoder;

import net.bdew.ae2stuff.misc.SlotClickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotFakeCrafting extends Slot implements SlotClickable {
    private final Runnable onChanged;

    public SlotFakeCrafting(IInventory inventory, int index, int x, int y, Runnable onChanged) {
        super(inventory, index, x, y);
        this.onChanged = onChanged;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack onClick(ClickType clickType, int button, PlayerEntity player) {
        ItemStack held = player.inventory.getItemStack();
        ItemStack newStack = ItemStack.EMPTY;
        if (!held.isEmpty()) {
            newStack = held.copy();
            newStack.setCount(1);
        }
        if (!ItemStack.areItemStacksEqual(newStack, inventory.getStackInSlot(getSlotIndex()))) {
            inventory.setInventorySlotContents(getSlotIndex(), newStack);
            onChanged.run();
        }
        return held;
    }
}
