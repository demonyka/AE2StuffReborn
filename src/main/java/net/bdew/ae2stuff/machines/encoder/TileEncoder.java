package net.bdew.ae2stuff.machines.encoder;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
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
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.List;

public class TileEncoder extends InventoryGridTile {
    public static final int SLOT_RECIPE_COUNT = 9;
    public static final int SLOT_RESULT = 9;
    public static final int SLOT_PATTERNS = 10;
    public static final int SLOT_ENCODED = 11;

    private static final int[] SIDED_SLOTS = {SLOT_PATTERNS};

    // Items a client asks for that are not in ME storage keep their tag only up to this size
    private static final int MAX_UNTRUSTED_TAG_LENGTH = 4096;

    // The recipe search is expensive in big packs, so it runs once per change of the recipe slots
    @Nullable
    private ICraftingRecipe recipe;
    @Nullable
    private RecipeManager recipeSource;
    private boolean recipeKnown;

    public TileEncoder() {
        super(Registration.ENCODER_TILE.get(), 12);
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
     * The recipe for the current grid. A new recipe manager, after /reload or a script reload, triggers a new search.
     */
    @Nullable
    private ICraftingRecipe findRecipe() {
        RecipeManager manager = world.getRecipeManager();
        if (!recipeKnown || manager != recipeSource) {
            recipe = manager.getRecipe(IRecipeType.CRAFTING, makeCraftingInventory(), world).orElse(null);
            recipeSource = manager;
            recipeKnown = true;
        }
        return recipe;
    }

    /**
     * Updates the result slot from the recipe slots.
     */
    public void updateRecipe() {
        ICraftingRecipe current = findRecipe();
        setInventorySlotContents(SLOT_RESULT, current != null ? current.getCraftingResult(makeCraftingInventory()) : ItemStack.EMPTY);
    }

    /**
     * Refreshes the result when the recipes changed since it was computed.
     */
    public void revalidateRecipe() {
        if (world != null && !world.isRemote && (!recipeKnown || recipeSource != world.getRecipeManager()))
            updateRecipe();
    }

    /**
     * Sets the recipe slots from the ingredient options sent by JEI, with a single recipe search.
     */
    public void setRecipeFromNetwork(List<List<ItemStack>> slots) {
        for (int i = 0; i < SLOT_RECIPE_COUNT; i++) {
            List<ItemStack> options = i < slots.size() ? slots.get(i) : null;
            inv.set(i, options == null || options.isEmpty() ? ItemStack.EMPTY : findMatchingRecipeStack(options));
        }
        recipeKnown = false;
        updateRecipe();
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < SLOT_RECIPE_COUNT)
            recipeKnown = false;
        super.setInventorySlotContents(slot, stack);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (slot < SLOT_RECIPE_COUNT)
            recipeKnown = false;
        return super.decrStackSize(slot, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        if (slot < SLOT_RECIPE_COUNT)
            recipeKnown = false;
        return super.removeStackFromSlot(slot);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        recipeKnown = false;
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
        if (getStackInSlot(SLOT_RESULT).isEmpty() || isRecipeEmpty() || getStackInSlot(SLOT_PATTERNS).isEmpty())
            return ItemStack.EMPTY;
        ICraftingRecipe current = findRecipe();
        if (current == null)
            return ItemStack.EMPTY;
        // The output comes from the recipe itself, the result slot can be older than a recipe reload
        ItemStack output = current.getCraftingResult(makeCraftingInventory());
        if (output.isEmpty())
            return ItemStack.EMPTY;
        return AE2Defs.api().crafting().encodeCraftingPattern(null, current, getRecipeStacks(), output, true);
    }

    public ItemStack findMatchingRecipeStack(List<ItemStack> stacks) {
        IGridNode gridNode = getNode();
        IGrid grid = gridNode != null ? gridNode.getGrid() : null;
        if (grid != null) {
            IItemStorageChannel channel = AE2Defs.api().storage().getStorageChannel(IItemStorageChannel.class);
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
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
        }
        // Get the first variant if we can't find any matches
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty())
                return untrustedGhost(stack);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack untrustedGhost(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        CompoundNBT tag = copy.getTag();
        if (tag != null && tag.toString().length() > MAX_UNTRUSTED_TAG_LENGTH)
            copy.setTag(null);
        return copy;
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
