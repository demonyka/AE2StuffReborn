package net.bdew.ae2stuff.misc;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import net.bdew.ae2stuff.AE2Defs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public final class UpgradeableHelper {
    private UpgradeableHelper() {
    }

    public static INetworkTool getNetworkToolObj(ItemStack stack, int slot, TileEntity te) {
        return (INetworkTool) ((IGuiItem) stack.getItem()).getGuiObject(stack, slot, te.getWorld(), te.getPos());
    }

    /**
     * @return the player inventory slot with a network tool, or -1
     */
    public static int findNetworktoolStack(PlayerEntity player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (AE2Defs.items().networkTool().isSameAs(player.inventory.getStackInSlot(i)))
                return i;
        }
        return -1;
    }
}
