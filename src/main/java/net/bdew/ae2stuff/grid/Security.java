package net.bdew.ae2stuff.grid;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.ISecurityGrid;
import net.bdew.ae2stuff.AE2Defs;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

public final class Security {
    private Security() {
    }

    public static int getPlayerId(PlayerEntity player) {
        return AE2Defs.api().registries().players().getID(player);
    }

    public static boolean playerHasPermission(@Nullable IGrid grid, int playerId, SecurityPermissions permission) {
        if (grid == null)
            return true;
        ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        if (security == null || !security.isAvailable())
            return true;
        return security.hasPermission(playerId, permission);
    }
}
