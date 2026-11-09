package net.bdew.ae2stuff.grid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A grid tile with an inventory. The machines with an inventory also keep all their data when broken.
 */
public abstract class InventoryGridTile extends GridTile implements ISidedInventory {
    private static final String ITEM_DATA_TAG = "data";

    protected NonNullList<ItemStack> inv;

    // One handler per side, the last entry is for a null side
    private final List<LazyOptional<IItemHandler>> handlers = new ArrayList<>();

    protected InventoryGridTile(TileEntityType<?> type, int size) {
        super(type);
        inv = NonNullList.withSize(size, ItemStack.EMPTY);
        for (Direction side : Direction.values())
            handlers.add(LazyOptional.of(() -> new SidedInvWrapper(this, side)));
        handlers.add(LazyOptional.of(() -> new SidedInvWrapper(this, null)));
    }

    @Override
    protected void saveData(CompoundNBT tag) {
        super.saveData(tag);
        ListNBT items = new ListNBT();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);
            if (!stack.isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putByte("Slot", (byte) i);
                stack.write(itemTag);
                items.add(itemTag);
            }
        }
        tag.put("Items", items);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        inv = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        ListNBT items = tag.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundNBT itemTag = items.getCompound(i);
            int slot = itemTag.getByte("Slot");
            if (slot >= 0 && slot < inv.size())
                inv.set(slot, ItemStack.read(itemTag));
        }
    }

    private CompoundNBT writeItemData() {
        CompoundNBT data = new CompoundNBT();
        saveData(data);
        // The grid node stays with the world, a placed copy has to join the network on its own
        data.remove("ae_node");
        return data;
    }

    public final void saveToItem(ItemStack stack) {
        CompoundNBT data = writeItemData();
        // A machine that holds nothing must stack with crafted ones
        TileEntity pristine = getType().create();
        if (pristine instanceof InventoryGridTile && data.equals(((InventoryGridTile) pristine).writeItemData()))
            return;
        stack.getOrCreateTag().put(ITEM_DATA_TAG, data);
    }

    public final void loadFromItem(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains(ITEM_DATA_TAG))
            loadData(tag.getCompound(ITEM_DATA_TAG));
    }

    // IInventory

    @Override
    public int getSizeInventory() {
        return inv.size();
    }

    @Override
    public boolean isEmpty() {
        return inv.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.get(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack = inv.get(slot);
        if (stack.isEmpty())
            return stack;
        ItemStack result = stack.split(count);
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack stack = ItemStackHelper.getAndRemove(inv, slot);
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inv.set(slot, stack);
        markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
    }

    @Override
    public void clear() {
        inv = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        markDirty();
    }

    // ISidedInventory

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[inv.size()];
        for (int i = 0; i < slots.length; i++)
            slots[i] = i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, @Nullable Direction side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        return true;
    }

    // Capabilities

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !removed)
            return handlers.get(side != null ? side.ordinal() : handlers.size() - 1).cast();
        return super.getCapability(capability, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        handlers.forEach(LazyOptional::invalidate);
    }
}
