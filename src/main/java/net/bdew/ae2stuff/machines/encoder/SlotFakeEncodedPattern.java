package net.bdew.ae2stuff.machines.encoder;

import net.bdew.ae2stuff.misc.ItemUtils;
import net.bdew.ae2stuff.misc.SlotClickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotFakeEncodedPattern extends Slot implements SlotClickable {
    private final TileEncoder te;

    public SlotFakeEncodedPattern(TileEncoder te, int index, int x, int y) {
        super(te, index, x, y);
        this.te = te;
    }

    @Override
    public ItemStack onClick(ClickType clickType, int button, PlayerEntity player) {
        ItemStack encoded = te.getStackInSlot(getSlotIndex());
        if (!encoded.isEmpty()) {
            if (clickType == ClickType.PICKUP) {
                if (player.inventory.getItemStack().isEmpty()) {
                    player.inventory.setItemStack(encoded.copy());
                    te.decrStackSize(TileEncoder.SLOT_PATTERNS, 1);
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (ItemUtils.addStackToSlots(encoded.copy(), player.inventory, 0, player.inventory.getSizeInventory(), true).isEmpty())
                    te.decrStackSize(TileEncoder.SLOT_PATTERNS, 1);
            }
        }
        return player.inventory.getItemStack();
    }
}
