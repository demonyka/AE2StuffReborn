package net.bdew.ae2stuff.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class BaseContainer extends Container {
    private final IInventory inventory;

    protected BaseContainer(ContainerType<?> type, int id, IInventory inventory) {
        super(type, id);
        this.inventory = inventory;
    }

    protected void bindPlayerInventory(PlayerInventory inv, int xOffs, int yOffsInv, int yOffsHotbar) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++)
                addSlot(new Slot(inv, j + i * 9 + 9, xOffs + j * 18, yOffsInv + i * 18));
        }
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, xOffs + i * 18, yOffsHotbar));
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, ClickType clickType, PlayerEntity player) {
        if (slotNum >= 0 && slotNum < inventorySlots.size()) {
            Slot slot = getSlot(slotNum);
            if (slot instanceof SlotClickable)
                return ((SlotClickable) slot).onClick(clickType, button, player);
        }
        return super.slotClick(slotNum, button, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = getSlot(index);
        ItemStack stack = slot.getStack();
        if (slot.inventory == player.inventory) {
            stack = ItemUtils.addStackToSlots(stack, inventory, 0, inventory.getSizeInventory(), true);
        } else {
            stack = ItemUtils.addStackToSlots(stack, player.inventory, 9, player.inventory.mainInventory.size(), true);
            // Only put in hotbar if the rest is full
            if (!stack.isEmpty())
                stack = ItemUtils.addStackToSlots(stack, player.inventory, 0, 9, true);
        }
        slot.putStack(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return inventory.isUsableByPlayer(player);
    }
}
