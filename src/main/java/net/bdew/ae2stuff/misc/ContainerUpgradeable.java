package net.bdew.ae2stuff.misc;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * Container with upgrade slots. A network tool in the player inventory adds its toolbox slots.
 */
public abstract class ContainerUpgradeable extends BaseContainer {
    private int netToolSlot = -1;
    @Nullable
    private INetworkTool netTool;
    private IInventory upgradeInventory;

    protected ContainerUpgradeable(ContainerType<?> type, int id, IInventory inventory) {
        super(type, id, inventory);
    }

    protected void initUpgradeable(TileEntity te, IInventory upgrades, PlayerEntity player, int baseX, int baseY) {
        upgradeInventory = upgrades;
        netToolSlot = UpgradeableHelper.findNetworktoolStack(player);
        if (netToolSlot >= 0) {
            netTool = UpgradeableHelper.getNetworkToolObj(player.inventory.getStackInSlot(netToolSlot), netToolSlot, te);
            IInventory toolbox = new InventoryHandlerAdapter(netTool.getInventory());
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++)
                    addSlot(new SlotValidating(toolbox, i + j * 3, baseX + i * 18, baseY + j * 18));
            }
        }
    }

    public boolean hasToolbox() {
        return netTool != null;
    }

    /**
     * The toolbox slots edit the network tool stack found when the GUI opened. Once that stack left its slot,
     * by dropping it for example, any further change would duplicate or delete the cards in it.
     */
    private boolean isToolboxIntact(PlayerEntity player) {
        // Client stacks are replaced on every sync, only the server copy can be compared
        if (netTool == null || player.world.isRemote)
            return true;
        ItemStack current = player.inventory.getStackInSlot(netToolSlot);
        return !current.isEmpty() && current == netTool.getItemStack();
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return super.canInteractWith(player) && isToolboxIntact(player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = getSlot(index);
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeModule && ((IUpgradeModule) stack.getItem()).getType(stack) != null) {
            // Is an upgrade
            if (slot.inventory != upgradeInventory) {
                // If it's not in upgrade inventory try to move it there
                slot.putStack(ItemUtils.addStackToSlots(stack, upgradeInventory, 0, upgradeInventory.getSizeInventory(), true));
            } else if (netTool != null) {
                // Otherwise if we have a network tool, move it there
                slot.putStack(ItemUtils.addStackToHandler(stack, netTool.getInventory()));
            } else {
                // Otherwise let the default handler move it to player inventory
                super.transferStackInSlot(player, index);
            }
        } else {
            // Otherwise let the default handler work
            super.transferStackInSlot(player, index);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, ClickType clickType, PlayerEntity player) {
        if (slotNum > 0 && slotNum < inventorySlots.size() && netToolSlot >= 0) {
            Slot slot = getSlot(slotNum);
            if (slot.inventory == player.inventory && slot.getSlotIndex() == netToolSlot)
                return ItemStack.EMPTY;
        }
        if (clickType == ClickType.SWAP && button == netToolSlot)
            return ItemStack.EMPTY;
        return super.slotClick(slotNum, button, clickType, player);
    }
}
