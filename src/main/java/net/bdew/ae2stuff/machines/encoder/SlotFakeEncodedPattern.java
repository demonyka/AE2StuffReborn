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

    // All clicks go through onClick, this keeps vanilla paths like double-click collecting away from the fake stack
    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onClick(ClickType clickType, int button, PlayerEntity player) {
        // A recipe removed by /reload must not turn a blank pattern into a dead one
        te.revalidateRecipe();
        ItemStack encoded = te.getStackInSlot(getSlotIndex()).copy();
        if (!encoded.isEmpty() && !te.getStackInSlot(TileEncoder.SLOT_PATTERNS).isEmpty()) {
            if (clickType == ClickType.PICKUP) {
                if (player.inventory.getItemStack().isEmpty() && !te.decrStackSize(TileEncoder.SLOT_PATTERNS, 1).isEmpty())
                    player.inventory.setItemStack(encoded);
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (ItemUtils.addStackToSlots(encoded, player.inventory, 0, player.inventory.mainInventory.size(), true).isEmpty())
                    te.decrStackSize(TileEncoder.SLOT_PATTERNS, 1);
            }
        }
        return player.inventory.getItemStack();
    }
}
