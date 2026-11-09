package net.bdew.ae2stuff.client;

import net.bdew.ae2stuff.Registration;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::clientSetup);
        MinecraftForge.EVENT_BUS.register(OverlayRenderHandler.class);
        MinecraftForge.EVENT_BUS.register(MouseEventHandler.class);
        MinecraftForge.EVENT_BUS.register(VisualiserOverlayRender.class);
        OverlayRenderHandler.register(new WirelessOverlayRender());
        OverlayRenderHandler.register(new VisualiserOverlayRender());
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (Registration.ENCODER_CONTAINER != null)
                ScreenManager.registerFactory(Registration.ENCODER_CONTAINER.get(), GuiEncoder::new);
            if (Registration.GROWER_CONTAINER != null)
                ScreenManager.registerFactory(Registration.GROWER_CONTAINER.get(), GuiGrower::new);
            if (Registration.INSCRIBER_CONTAINER != null)
                ScreenManager.registerFactory(Registration.INSCRIBER_CONTAINER.get(), GuiInscriber::new);
        });
    }
}
