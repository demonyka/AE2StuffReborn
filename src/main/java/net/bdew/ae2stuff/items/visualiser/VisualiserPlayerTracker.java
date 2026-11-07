package net.bdew.ae2stuff.items.visualiser;

import net.bdew.ae2stuff.misc.PosAndDimension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Limits how often network data is sent to each player.
 */
public final class VisualiserPlayerTracker {
    private static final Map<PlayerEntity, Entry> MAP = new HashMap<>();

    private VisualiserPlayerTracker() {
    }

    private static final class Entry {
        final PosAndDimension loc;
        final long last;

        Entry(PosAndDimension loc, long last) {
            this.loc = loc;
            this.last = last;
        }
    }

    public static boolean needToUpdate(PlayerEntity player, PosAndDimension loc) {
        long now = player.world.getGameTime();
        Entry last = MAP.get(player);
        if (last == null || !last.loc.equals(loc) || last.last < now - 100) {
            MAP.put(player, new Entry(loc, now));
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void handleServerStarting(FMLServerStartingEvent event) {
        MAP.clear();
    }

    @SubscribeEvent
    public static void handlePlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        MAP.remove(event.getPlayer());
    }

    @SubscribeEvent
    public static void handlePlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        MAP.remove(event.getPlayer());
    }

    @SubscribeEvent
    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        MAP.remove(event.getPlayer());
    }
}
