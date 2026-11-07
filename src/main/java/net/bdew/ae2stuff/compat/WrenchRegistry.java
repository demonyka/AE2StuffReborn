package net.bdew.ae2stuff.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WrenchRegistry {
    private static final List<WrenchHandler> REGISTRY = new CopyOnWriteArrayList<>();

    private WrenchRegistry() {
    }

    public static void init() {
        REGISTRY.add(new AEWrenchHandler());
    }

    @Nullable
    public static WrenchHandler findWrench(PlayerEntity player, ItemStack stack, BlockPos pos) {
        for (WrenchHandler handler : REGISTRY) {
            if (handler.canWrench(player, stack, pos))
                return handler;
        }
        return null;
    }
}
