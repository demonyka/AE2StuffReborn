package net.bdew.ae2stuff.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;

/**
 * A slot that handles clicks by itself, see {@link BaseContainer#slotClick}.
 */
public interface SlotClickable {
    ItemStack onClick(ClickType clickType, int button, PlayerEntity player);
}
