package net.bdew.ae2stuff.misc;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class UpgradeInventory extends Inventory {
    private final String name;
    private final TileEntity parent;
    private final Set<Upgrades> kinds;
    private final Map<Upgrades, Integer> cards = new EnumMap<>(Upgrades.class);

    public UpgradeInventory(String name, TileEntity parent, int size, Upgrades first, Upgrades... rest) {
        super(size);
        this.name = name;
        this.parent = parent;
        this.kinds = EnumSet.of(first, rest);
    }

    public int cards(Upgrades kind) {
        return cards.getOrDefault(kind, 0);
    }

    private void updateUpgradeCounts() {
        cards.clear();
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeModule) {
                Upgrades type = ((IUpgradeModule) stack.getItem()).getType(stack);
                if (type != null)
                    cards.merge(type, 1, Integer::sum);
            }
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IUpgradeModule
                && kinds.contains(((IUpgradeModule) stack.getItem()).getType(stack));
    }

    @Override
    public void markDirty() {
        updateUpgradeCounts();
        super.markDirty();
        if (parent.getWorld() != null && !parent.getWorld().isRemote)
            parent.getWorld().markChunkDirty(parent.getPos(), parent);
    }

    public void save(CompoundNBT tag) {
        ListNBT items = new ListNBT();
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putByte("Slot", (byte) i);
                stack.write(itemTag);
                items.add(itemTag);
            }
        }
        tag.put(name, items);
    }

    public void load(CompoundNBT tag) {
        clear();
        ListNBT items = tag.getList(name, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundNBT itemTag = items.getCompound(i);
            int slot = itemTag.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory())
                super.setInventorySlotContents(slot, ItemStack.read(itemTag));
        }
        updateUpgradeCounts();
    }
}
