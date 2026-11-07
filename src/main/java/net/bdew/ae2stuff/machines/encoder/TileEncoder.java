package net.bdew.ae2stuff.machines.encoder;

import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.bdew.ae2stuff.AE2Defs;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.Tuning;
import net.bdew.ae2stuff.grid.InventoryGridTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.Direction;

import java.util.List;
import java.util.Optional;

public class TileEncoder extends InventoryGridTile {
    public static final int SLOT_RECIPE_COUNT = 9;
    public static final int SLOT_RESULT = 9;
    public static final int SLOT_PATTERNS = 10;
    public static final int SLOT_ENCODED = 11;

    private static final int[] SIDED_SLOTS = {SLOT_PATTERNS};

    public TileEncoder() {
        super(Registration.ENCODER_TILE.get(), 12);
    }

    private ItemStack getResult() {
        return getStackInSlot(SLOT_RESULT);
    }

    private ItemStack[] getRecipeStacks() {
        ItemStack[] stacks = new ItemStack[SLOT_RECIPE_COUNT];
        for (int i = 0; i < SLOT_RECIPE_COUNT; i++)
            stacks[i] = getStackInSlot(i);
        return stacks;
    }

    private boolean isRecipeEmpty() {
        for (ItemStack stack : getRecipeStacks()) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    private CraftingInventory makeCraftingInventory() {
        CraftingInventory crafting = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity player) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < SLOT_RECIPE_COUNT; i++)
            crafting.setInventorySlotContents(i, getStackInSlot(i));
        return crafting;
    }

    /**
     * Updates the result slot from the recipe slots.
     */
    public void updateRecipe() {
        CraftingInventory crafting = makeCraftingInventory();
        Optional<ICraftingRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, crafting, world);
        setInventorySlotContents(SLOT_RESULT, recipe.map(r -> r.getCraftingResult(crafting)).orElse(ItemStack.EMPTY));
    }

    @Override
    public void markDirty() {
        if (world != null && !world.isRemote)
            inv.set(SLOT_ENCODED, encodePattern()); // direct to prevent an infinite recursion
        super.markDirty();
    }

    /**
     * AE2 8.x patterns reference the recipe by id, so unlike on 1.12 they can't be written from the slots alone.
     */
    private ItemStack encodePattern() {
        if (getResult().isEmpty() || isRecipeEmpty() || getStackInSlot(SLOT_PATTERNS).isEmpty())
            return ItemStack.EMPTY;
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, makeCraftingInventory(), world)
                .map(recipe -> AE2Defs.api().crafting().encodeCraftingPattern(null, recipe, getRecipeStacks(), getResult(), true))
                .orElse(ItemStack.EMPTY);
    }

    public ItemStack findMatchingRecipeStack(List<ItemStack> stacks) {
        IItemStorageChannel channel = AE2Defs.api().storage().getStorageChannel(IItemStorageChannel.class);
        IStorageGrid storageGrid = node.getGrid().getCache(IStorageGrid.class);
        IItemList<IAEItemStack> storage = storageGrid.getInventory(channel).getStorageList();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty())
                continue;
            IAEItemStack found = storage.findPrecise(channel.createStack(stack));
            if (found != null) {
                ItemStack result = found.createItemStack();
                result.setCount(1);
                return result;
            }
        }
        // Get the first variant if we can't find any matches
        return stacks.get(0);
    }

    // Inventory stuff

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SIDED_SLOTS;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == SLOT_PATTERNS && AE2Defs.materials().blankPattern().isSameAs(stack);
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(Registration.ENCODER.get());
    }

    @Override
    public double getIdlePowerUsage() {
        return Tuning.ENCODER_IDLE_POWER.get();
    }

    @MENetworkEventSubscribe
    public void networkPowerStatusChange(MENetworkPowerStatusChange event) {
        setBlockActive(isNodeActive());
    }
}
