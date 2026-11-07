package net.bdew.ae2stuff.items.visualiser;

import java.util.Locale;

public enum VisualisationModes {
    FULL, NODES, CHANNELS, NONUM, P2P;

    public String translationKey() {
        return "ae2stuff.visualiser.mode." + name().toLowerCase(Locale.US);
    }

    public VisualisationModes next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public VisualisationModes prev() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static VisualisationModes byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : FULL;
    }
}
