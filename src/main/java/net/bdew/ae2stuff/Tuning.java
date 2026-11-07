package net.bdew.ae2stuff;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Port of tuning.cfg. Option names and defaults match the original file.
 */
public final class Tuning {
    private static final String FILE_NAME = AE2Stuff.MOD_ID + "-common.toml";

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue ENCODER_IDLE_POWER;

    public static final ForgeConfigSpec.DoubleValue GROWER_IDLE_POWER;
    public static final ForgeConfigSpec.IntValue GROWER_CYCLE_TICKS;
    public static final ForgeConfigSpec.DoubleValue GROWER_CYCLE_POWER;
    public static final ForgeConfigSpec.DoubleValue GROWER_POWER_CAPACITY;

    public static final ForgeConfigSpec.DoubleValue INSCRIBER_IDLE_POWER;
    public static final ForgeConfigSpec.DoubleValue INSCRIBER_CYCLE_POWER;
    public static final ForgeConfigSpec.DoubleValue INSCRIBER_POWER_CAPACITY;
    public static final ForgeConfigSpec.IntValue INSCRIBER_CYCLE_TICKS;

    public static final ForgeConfigSpec.DoubleValue WIRELESS_POWER_BASE;
    public static final ForgeConfigSpec.DoubleValue WIRELESS_POWER_DISTANCE_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        final double max = Double.MAX_VALUE;

        b.push("machines");

        b.push("encoder");
        enabled(b);
        ENCODER_IDLE_POWER = b.comment("Idle power draw (AE/t)").defineInRange("idlePower", 1D, 0D, max);
        b.pop();

        b.push("grower");
        enabled(b);
        GROWER_IDLE_POWER = b.comment("Idle power draw (AE/t)").defineInRange("idlePower", 0D, 0D, max);
        GROWER_CYCLE_TICKS = b.comment("Length of cycle, increase to slow down (in ticks)").defineInRange("cycleTicks", 1, 1, Integer.MAX_VALUE);
        GROWER_CYCLE_POWER = b.comment("Power used per growth cycle (AE)").defineInRange("cyclePower", 100D, 0D, max);
        GROWER_POWER_CAPACITY = b.comment("Internal power store (AE)").defineInRange("powerCapacity", 10000D, 1D, max);
        b.pop();

        b.push("inscriber");
        enabled(b);
        INSCRIBER_IDLE_POWER = b.comment("Idle power draw (AE/t)").defineInRange("idlePower", 0D, 0D, max);
        INSCRIBER_CYCLE_POWER = b.comment("Power used per cycle (AE)").defineInRange("cyclePower", 1000D, 0D, max);
        INSCRIBER_POWER_CAPACITY = b.comment("Internal power store (AE)").defineInRange("powerCapacity", 5000D, 1D, max);
        INSCRIBER_CYCLE_TICKS = b.comment("Ticks per cycle").defineInRange("cycleTicks", 100, 1, Integer.MAX_VALUE);
        b.pop();

        b.push("wireless");
        enabled(b);
        WIRELESS_POWER_BASE = b.comment("Power use = powerBase + powerDistanceMultiplier * Distance^2 (AE/t)").defineInRange("powerBase", 10D, 0D, max);
        WIRELESS_POWER_DISTANCE_MULTIPLIER = b.comment("Power use = powerBase + powerDistanceMultiplier * Distance^2 (AE/t)").defineInRange("powerDistanceMultiplier", 1D, 0D, max);
        b.pop();

        b.pop();
        SPEC = b.build();
    }

    private Tuning() {
    }

    private static void enabled(ForgeConfigSpec.Builder b) {
        b.comment("Registers the machine. Has to be the same on the client and the server.").worldRestart().define("enabled", true);
    }

    static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, FILE_NAME);
    }

    /**
     * Forge loads common configs after registry events, but a disabled machine must not be registered at all.
     * This reads the flag straight from the file, a missing file or key means the default of enabled.
     */
    static boolean isEnabled(String machine) {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path))
            return true;
        try (CommentedFileConfig config = CommentedFileConfig.builder(path).build()) {
            config.load();
            return config.getOrElse("machines." + machine + ".enabled", true);
        } catch (RuntimeException e) {
            AE2Stuff.LOG.warn("Failed reading {}, treating {} as enabled", path, machine, e);
            return true;
        }
    }
}
