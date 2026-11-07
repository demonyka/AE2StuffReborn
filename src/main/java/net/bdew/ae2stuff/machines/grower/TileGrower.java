package net.bdew.ae2stuff.machines.grower;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.networking.GridNotification;
import net.bdew.ae2stuff.AE2Defs;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.Tuning;
import net.bdew.ae2stuff.grid.PoweredTile;
import net.bdew.ae2stuff.misc.ItemUtils;
import net.bdew.ae2stuff.misc.UpgradeInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class TileGrower extends PoweredTile {
    private static final Item REDSTONE_DUST = Items.REDSTONE;
    private static final Item NETHER_QUARTZ = Items.QUARTZ;

    public final UpgradeInventory upgrades = new UpgradeInventory("upgrades", this, 3, Upgrades.SPEED);

    public TileGrower() {
        super(Registration.GROWER_TILE.get(), 3 * 9);
    }

    private static IItemDefinition chargedCertusQuartz() {
        return AE2Defs.materials().certusQuartzCrystalCharged();
    }

    private static IItemDefinition fluixCrystal() {
        return AE2Defs.materials().fluixCrystal();
    }

    @Override
    public double getPowerCapacity() {
        return Tuning.GROWER_POWER_CAPACITY.get();
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(Registration.GROWER.get());
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        if (world.getGameTime() % Tuning.GROWER_CYCLE_TICKS.get() == 0 && isAwake()) {
            boolean hadWork = false;
            int speed = upgrades.cards(Upgrades.SPEED);
            double needPower = Tuning.GROWER_CYCLE_POWER.get() * (1 + speed);
            if (powerStored >= needPower) {
                int certPos = findChargedCertus();

                for (int slot = 0; slot < inv.size(); slot++) {
                    ItemStack stack = inv.get(slot);
                    if (!stack.isEmpty() && stack.getItem() instanceof IGrowableCrystal) {
                        IGrowableCrystal crystal = (IGrowableCrystal) stack.getItem();
                        ItemStack grown = stack;
                        for (int i = 0; i <= speed; i++)
                            grown = crystal.triggerGrowth(stack);
                        setInventorySlotContents(slot, grown);
                        hadWork = true;
                    }
                }

                int redstonePos = ItemUtils.findItemInInventory(this, REDSTONE_DUST);
                int netherPos = ItemUtils.findItemInInventory(this, NETHER_QUARTZ);
                if (certPos >= 0 && redstonePos >= 0 && netherPos >= 0 && hasFluixOutputSpace()) {
                    decrStackSize(certPos, 1);
                    decrStackSize(netherPos, 1);
                    decrStackSize(redstonePos, 1);
                    ItemUtils.addStackToSlots(fluixCrystal().stack(2), this, 0, getSizeInventory(), false);
                    hadWork = true;
                }
            }

            if (hadWork)
                usePower(needPower);
            else
                sleep();

            requestPowerIfNeeded();
        }
    }

    private int findChargedCertus() {
        for (int slot = 0; slot < inv.size(); slot++) {
            ItemStack stack = inv.get(slot);
            if (!stack.isEmpty() && chargedCertusQuartz().isSameAs(stack))
                return slot;
        }
        return -1;
    }

    private boolean hasFluixOutputSpace() {
        for (ItemStack stack : inv) {
            if (stack.isEmpty() || (fluixCrystal().isSameAs(stack) && stack.getCount() <= stack.getMaxStackSize() - 2))
                return true;
        }
        return false;
    }

    @Override
    protected void saveData(CompoundNBT tag) {
        super.saveData(tag);
        upgrades.save(tag);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        upgrades.load(tag);
    }

    @Override
    public void onGridNotification(GridNotification notification) {
        wakeup();
    }

    @Override
    public void markDirty() {
        wakeup();
        super.markDirty();
    }

    @Override
    public double getIdlePowerUsage() {
        return Tuning.GROWER_IDLE_POWER.get();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return !stack.isEmpty() && (
                stack.getItem() instanceof IGrowableCrystal
                        || stack.getItem() == NETHER_QUARTZ
                        || stack.getItem() == REDSTONE_DUST
                        || chargedCertusQuartz().isSameAs(stack)
        );
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        return !isItemValidForSlot(slot, stack);
    }
}
