package net.bdew.ae2stuff.grid;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkPowerStorage;
import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Machine with an internal AE buffer that sleeps while it has nothing to do.
 * The block shows as active while the machine is awake.
 */
public abstract class PoweredTile extends InventoryGridTile implements IAEPowerStorage {
    protected double powerStored;
    private boolean postedReq;

    private boolean sleeping;
    private final int forceWakeupOn = new Random().nextInt(100);

    protected PoweredTile(TileEntityType<?> type, int size) {
        super(type, size);
    }

    public abstract double getPowerCapacity();

    public double getPowerStored() {
        return powerStored;
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        // This should prevent tiles from getting stuck sleeping forever
        if (world.getGameTime() % 100 == forceWakeupOn)
            wakeup();
    }

    public boolean isAwake() {
        return !sleeping;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void sleep() {
        sleeping = true;
    }

    public void wakeup() {
        sleeping = false;
    }

    // The forced wakeup above is usually undone in the same tick, so the block is only updated once the tick is over
    @Override
    protected void serverTickEnd() {
        super.serverTickEnd();
        BlockState state = getBlockState();
        if (state.getBlock() instanceof MachineBlock && state.get(MachineBlock.ACTIVE) != isAwake())
            setBlockActive(isAwake());
    }

    @Override
    protected void saveData(CompoundNBT tag) {
        super.saveData(tag);
        tag.putDouble("power", powerStored);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        powerStored = tag.getDouble("power");
    }

    protected void usePower(double amount) {
        powerStored -= amount;
        markSaveDirty();
    }

    protected void requestPowerIfNeeded() {
        if (node != null && node.isActive()) {
            double capacity = getPowerCapacity();
            if (!postedReq && powerStored / capacity < 0.9D) {
                postedReq = true;
                safePostEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
            } else if (powerStored / capacity < 0.5D) {
                IEnergyGrid net = node.getGrid().getCache(IEnergyGrid.class);
                if (net.getStoredPower() / net.getMaxStoredPower() > 0.2D) {
                    double amount = Math.min(capacity - powerStored, net.getMaxStoredPower() * 0.1D);
                    double drawn = net.extractAEPower(amount, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    if (drawn > 0) {
                        powerStored += drawn;
                        markSaveDirty();
                        wakeup();
                    }
                }
            }
        }
    }

    // IAEPowerStorage

    @Override
    public double getAECurrentPower() {
        return powerStored;
    }

    @Override
    public double getAEMaxPower() {
        return getPowerCapacity();
    }

    @Override
    public double injectAEPower(double amount, Actionable mode) {
        double canStore = MathHelper.clamp(amount, 0D, getPowerCapacity() - powerStored);
        if (mode == Actionable.MODULATE && canStore > 0) {
            powerStored += canStore;
            markSaveDirty();
            postedReq = false;
            wakeup();
        }
        return amount - canStore;
    }

    @Override
    public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
        return 0;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.WRITE;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }
}
