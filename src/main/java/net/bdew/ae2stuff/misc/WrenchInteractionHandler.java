package net.bdew.ae2stuff.misc;

import net.bdew.ae2stuff.compat.WrenchRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * In AE2 1.12 the quartz wrench let sneak-clicks through to the block with doesSneakBypassUse, AE2 8.x dropped that.
 * Without this the block would never see the click that dismantles it.
 */
public final class WrenchInteractionHandler {
    private WrenchInteractionHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (event.getUseBlock() == Event.Result.DEFAULT && event.getHand() == Hand.MAIN_HAND && player.isSneaking()
                && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof MachineBlock
                && WrenchRegistry.findWrench(player, player.inventory.getCurrentItem(), event.getPos()) != null) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }
}
