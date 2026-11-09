package net.bdew.ae2stuff.items.visualiser;

import net.bdew.ae2stuff.misc.PosAndDimension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Limits how often network data is sent to each player.
 */
public final class VisualiserPlayerTracker {
    // Keyed by UUID because respawning creates a new player object
    private static final Map<UUID, Entry> MAP = new HashMap<>();

    private VisualiserPlayerTracker() {
    }

    private static final class Entry {
        @Nullable
        final PosAndDimension loc;
        final long last;
        final boolean empty;

        Entry(@Nullable PosAndDimension loc, long last, boolean empty) {
            this.loc = loc;
            this.last = last;
            this.empty = empty;
        }
    }

    public static boolean needToUpdate(PlayerEntity player, PosAndDimension loc) {
        long now = player.world.getGameTime();
        Entry last = MAP.get(player.getUniqueID());
        // A new location updates right away, but switching between tools still can't walk a grid every tick
        if (last == null || last.last < now - 100 || ((last.empty || !Objects.equals(last.loc, loc)) && last.last < now - 10)) {
            MAP.put(player.getUniqueID(), new Entry(loc, now, false));
            return true;
        }
        return false;
    }

    /**
     * @return true if the player may still have network data that should now be cleared
     */
    public static boolean needToClear(PlayerEntity player) {
        Entry last = MAP.get(player.getUniqueID());
        if (last != null && last.empty)
            return false;
        // Keeps the time of the last grid walk, so the throttle still applies once there is a network again
        MAP.put(player.getUniqueID(), last != null ? new Entry(last.loc, last.last, true) : new Entry(null, Long.MIN_VALUE, true));
        return true;
    }

    @SubscribeEvent
    public static void handleServerStarting(FMLServerStartingEvent event) {
        MAP.clear();
    }

    @SubscribeEvent
    public static void handlePlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        MAP.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public static void handlePlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        MAP.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        MAP.remove(event.getPlayer().getUniqueID());
    }
}
