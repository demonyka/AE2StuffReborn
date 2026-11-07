package net.bdew.ae2stuff.machines.inscriber;

import appeng.api.config.Upgrades;
import appeng.api.features.InscriberProcessType;
import appeng.api.networking.GridNotification;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.tile.misc.InscriberRecipes;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.Tuning;
import net.bdew.ae2stuff.grid.PoweredTile;
import net.bdew.ae2stuff.misc.ItemUtils;
import net.bdew.ae2stuff.misc.UpgradeInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class TileInscriber extends PoweredTile {
    public static final int SLOT_TOP = 0;
    public static final int SLOT_MIDDLE = 1;
    public static final int SLOT_BOTTOM = 2;
    public static final int SLOT_OUTPUT = 3;

    public final UpgradeInventory upgrades = new UpgradeInventory("upgrades", this, 5, Upgrades.SPEED);

    public float progress;
    private ItemStack output = ItemStack.EMPTY;
    public boolean topLocked = true;
    public boolean bottomLocked = true;

    public TileInscriber() {
        super(Registration.INSCRIBER_TILE.get(), 4);
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(Registration.INSCRIBER.get());
    }

    @Override
    public double getPowerCapacity() {
        return Tuning.INSCRIBER_POWER_CAPACITY.get();
    }

    private boolean isWorking() {
        return !output.isEmpty();
    }

    public void setLock(String slot, boolean locked) {
        switch (slot) {
            case "top":
                topLocked = locked;
                break;
            case "bottom":
                bottomLocked = locked;
                break;
            default:
                throw new IllegalArgumentException("Invalid slot");
        }
        markSaveDirty();
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        if (!isAwake())
            return;

        if (!isWorking()) {
            // No progress going - try starting
            InscriberRecipe recipe = findFinalRecipe();
            if (recipe != null) {
                output = recipe.getOutput().copy();
                progress = 0;
                decrStackSize(SLOT_MIDDLE, 1);
                if (recipe.getProcessType() == InscriberProcessType.PRESS) {
                    decrStackSize(SLOT_TOP, 1);
                    decrStackSize(SLOT_BOTTOM, 1);
                }
            }
            if (!isWorking()) // Failed - go sleep
                sleep();
        }

        if (isWorking()) {
            // Have something to do
            if (progress < 1) {
                // Not finished - try progressing
                float progressPerTick = (1F / Tuning.INSCRIBER_CYCLE_TICKS.get()) * (1 + upgrades.cards(Upgrades.SPEED));
                double powerNeeded = Tuning.INSCRIBER_CYCLE_POWER.get() * progressPerTick;
                if (powerStored >= powerNeeded) {
                    // Have enough power - consume it and add to progress
                    progress += progressPerTick;
                    usePower(powerNeeded);
                } else {
                    // Not enough power - sleep
                    sleep();
                }
            }
            if (progress >= 1) {
                // Finished - try to output
                ItemStack outputSlot = getStackInSlot(SLOT_OUTPUT);
                if (outputSlot.isEmpty() || (ItemUtils.isSameItem(outputSlot, output) && outputSlot.getCount() + output.getCount() <= outputSlot.getMaxStackSize())) {
                    // Can output - finish process
                    if (outputSlot.isEmpty()) {
                        setInventorySlotContents(SLOT_OUTPUT, output);
                    } else {
                        outputSlot.grow(output.getCount());
                        markDirty();
                    }
                    output = ItemStack.EMPTY;
                    progress = 0;
                } else {
                    // Can't output - switch to sleep mode
                    sleep();
                }
            }
            requestPowerIfNeeded();
        }
    }

    @Override
    protected void saveData(CompoundNBT tag) {
        super.saveData(tag);
        upgrades.save(tag);
        tag.putFloat("progress", progress);
        if (!output.isEmpty())
            tag.put("output", output.write(new CompoundNBT()));
        tag.putBoolean("topLocked", topLocked);
        tag.putBoolean("bottomLocked", bottomLocked);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        upgrades.load(tag);
        progress = tag.getFloat("progress");
        output = tag.contains("output") ? ItemStack.read(tag.getCompound("output")) : ItemStack.EMPTY;
        topLocked = !tag.contains("topLocked") || tag.getBoolean("topLocked");
        bottomLocked = !tag.contains("bottomLocked") || tag.getBoolean("bottomLocked");
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

    @Nullable
    private InscriberRecipe findFinalRecipe() {
        for (InscriberRecipe recipe : InscriberRecipes.getRecipes(world)) {
            if (isMatchingFullRecipe(recipe))
                return recipe;
        }
        return null;
    }

    private boolean isMatchingFullRecipe(InscriberRecipe recipe) {
        return !getStackInSlot(SLOT_MIDDLE).isEmpty()
                && recipe.getTopOptional().test(getStackInSlot(SLOT_TOP))
                && recipe.getBottomOptional().test(getStackInSlot(SLOT_BOTTOM))
                && recipe.getMiddleInput().test(getStackInSlot(SLOT_MIDDLE));
    }

    private static boolean matchesOptional(Ingredient ingredient, ItemStack stack) {
        return stack.isEmpty() || (!ingredient.hasNoMatchingItems() && ingredient.test(stack));
    }

    private static boolean isMatchingPartialRecipe(InscriberRecipe recipe, ItemStack top, ItemStack middle, ItemStack bottom) {
        return matchesOptional(recipe.getTopOptional(), top)
                && (middle.isEmpty() || recipe.getMiddleInput().test(middle))
                && matchesOptional(recipe.getBottomOptional(), bottom);
    }

    private boolean isValidPartialRecipe(ItemStack top, ItemStack middle, ItemStack bottom) {
        for (InscriberRecipe recipe : InscriberRecipes.getRecipes(world)) {
            if (isMatchingPartialRecipe(recipe, top, middle, bottom))
                return true;
        }
        return false;
    }

    @Override
    public double getIdlePowerUsage() {
        return Tuning.INSCRIBER_IDLE_POWER.get();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_TOP:
                return isValidPartialRecipe(stack, inv.get(SLOT_MIDDLE), inv.get(SLOT_BOTTOM));
            case SLOT_MIDDLE:
                return isValidPartialRecipe(inv.get(SLOT_TOP), stack, inv.get(SLOT_BOTTOM));
            case SLOT_BOTTOM:
                return isValidPartialRecipe(inv.get(SLOT_TOP), inv.get(SLOT_MIDDLE), stack);
            default:
                return false;
        }
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        switch (slot) {
            case SLOT_OUTPUT:
                return true;
            case SLOT_TOP:
                return !topLocked && isWorking() && inv.get(SLOT_MIDDLE).isEmpty();
            case SLOT_BOTTOM:
                return !bottomLocked && isWorking() && inv.get(SLOT_MIDDLE).isEmpty();
            default:
                return false;
        }
    }
}
