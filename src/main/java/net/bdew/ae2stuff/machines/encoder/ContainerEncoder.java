package net.bdew.ae2stuff.machines.encoder;

import net.bdew.ae2stuff.AE2Defs;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.misc.BaseContainer;
import net.bdew.ae2stuff.misc.SlotValidating;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ContainerEncoder extends BaseContainer {
    public final TileEncoder te;
    private final PlayerEntity player;
    private final Slot patternSlot;

    public ContainerEncoder(int id, PlayerInventory inventory, TileEncoder te) {
        super(Registration.ENCODER_CONTAINER.get(), id, te);
        this.te = te;
        this.player = inventory.player;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++)
                addSlot(new SlotFakeCrafting(te, x + y * 3, 10 + x * 18, 17 + y * 18, this::updateRecipe));
        }

        addSlot(new SlotFakeCraftingResult(te, TileEncoder.SLOT_RESULT, 104, 35));
        patternSlot = addSlot(new SlotValidating(te, TileEncoder.SLOT_PATTERNS, 143, 17));
        addSlot(new SlotFakeEncodedPattern(te, TileEncoder.SLOT_ENCODED, 143, 53));

        bindPlayerInventory(inventory, 8, 84, 142);
        te.revalidateRecipe();
    }

    public static ContainerEncoder fromNetwork(int id, PlayerInventory inventory, PacketBuffer data) {
        return new ContainerEncoder(id, inventory, (TileEncoder) inventory.player.world.getTileEntity(data.readBlockPos()));
    }

    public void updateRecipe() {
        te.updateRecipe();
    }

    private static boolean isEncodedPattern(ItemStack stack) {
        return AE2Defs.items().encodedPattern().isSameAs(stack);
    }

    private static ItemStack blankPatterns(int count) {
        return AE2Defs.materials().blankPattern().stack(count);
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, ClickType clickType, PlayerEntity player) {
        if (!canInteractWith(player))
            return ItemStack.EMPTY;
        if (slotNum >= 0 && slotNum < inventorySlots.size() && getSlot(slotNum) == patternSlot && button == 0 && clickType == ClickType.PICKUP) {
            ItemStack playerStack = player.inventory.getItemStack();
            ItemStack slotStack = patternSlot.getStack();
            if (isEncodedPattern(playerStack)) {
                if (slotStack.isEmpty()) {
                    patternSlot.putStack(blankPatterns(playerStack.getCount()));
                    player.inventory.setItemStack(ItemStack.EMPTY);
                    detectAndSendChanges();
                    return ItemStack.EMPTY;
                } else if (slotStack.getCount() + playerStack.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(playerStack.getCount());
                    patternSlot.onSlotChanged();
                    player.inventory.setItemStack(ItemStack.EMPTY);
                    detectAndSendChanges();
                    return ItemStack.EMPTY;
                }
            }
        }

        // This is a hacky workaround!
        // When a player changes the contents of a slot, isChangingQuantityOnly is set to true,
        // preventing updates to OTHER slots from being detected and sent back
        // Here i ensure changes are sent back before returning so the packet handler doesn't
        // get the opportunity to mess things up
        ItemStack result = super.slotClick(slotNum, button, clickType, player);
        detectAndSendChanges();
        return result;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot fromSlot = getSlot(index);
        ItemStack clickedStack = fromSlot.getStack();
        if (fromSlot.inventory == player.inventory && isEncodedPattern(clickedStack)) {
            ItemStack patternStack = patternSlot.getStack();
            if (patternStack.isEmpty()) {
                patternSlot.putStack(blankPatterns(clickedStack.getCount()));
                fromSlot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            } else if (patternStack.getCount() + clickedStack.getCount() <= patternStack.getMaxStackSize()) {
                patternStack.grow(clickedStack.getCount());
                patternSlot.onSlotChanged();
                fromSlot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
        }
        return super.transferStackInSlot(player, index);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (player instanceof ServerPlayerEntity && !te.isNodeActive())
            ((ServerPlayerEntity) player).closeScreen();
    }
}
