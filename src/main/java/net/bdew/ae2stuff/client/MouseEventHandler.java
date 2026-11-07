package net.bdew.ae2stuff.client;

import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.items.visualiser.ItemVisualiser;
import net.bdew.ae2stuff.items.visualiser.VisualisationModes;
import net.bdew.ae2stuff.network.MsgVisualisationMode;
import net.bdew.ae2stuff.network.NetHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MouseEventHandler {
    private MouseEventHandler() {
    }

    @SubscribeEvent
    public static void handleMouseEvent(InputEvent.MouseScrollEvent event) {
        if (event.getScrollDelta() == 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen != null)
            return;

        ClientPlayerEntity player = mc.player;
        if (player == null || !player.isSneaking())
            return;

        ItemStack stack = player.inventory.getCurrentItem();
        ItemVisualiser visualiser = Registration.VISUALISER.get();
        if (stack.getItem() == visualiser) {
            VisualisationModes mode = visualiser.getMode(stack);
            VisualisationModes newMode = event.getScrollDelta() > 0 ? mode.next() : mode.prev();
            visualiser.setMode(stack, newMode);
            NetHandler.sendToServer(new MsgVisualisationMode(newMode));
            event.setCanceled(true);
        }
    }
}
