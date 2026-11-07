package net.bdew.ae2stuff;

import appeng.api.config.Upgrades;
import net.bdew.ae2stuff.client.ClientSetup;
import net.bdew.ae2stuff.compat.WrenchRegistry;
import net.bdew.ae2stuff.items.visualiser.VisualiserPlayerTracker;
import net.bdew.ae2stuff.misc.WrenchInteractionHandler;
import net.bdew.ae2stuff.network.NetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AE2Stuff.MOD_ID)
public class AE2Stuff {
    public static final String MOD_ID = "ae2stuff";
    public static final Logger LOG = LogManager.getLogger("AE2 Stuff");

    public AE2Stuff() {
        Tuning.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.init(modBus);
        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(VisualiserPlayerTracker.class);
        MinecraftForge.EVENT_BUS.register(WrenchInteractionHandler.class);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NetHandler.init();
        WrenchRegistry.init();
        event.enqueueWork(() -> {
            if (Registration.GROWER != null)
                Upgrades.SPEED.registerItem(Registration.GROWER.get(), 3);
            if (Registration.INSCRIBER != null)
                Upgrades.SPEED.registerItem(Registration.INSCRIBER.get(), 5);
        });
    }
}
