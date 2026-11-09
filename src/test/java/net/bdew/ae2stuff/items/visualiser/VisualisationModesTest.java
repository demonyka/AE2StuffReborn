package net.bdew.ae2stuff.items.visualiser;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisualisationModesTest {
    private static final VisualisationModes[] MODES = VisualisationModes.values();

    @Test
    void nextWrapsAround() {
        assertEquals(VisualisationModes.NODES, VisualisationModes.FULL.next());
        assertEquals(VisualisationModes.FULL, MODES[MODES.length - 1].next());
    }

    @Test
    void prevWrapsAround() {
        assertEquals(MODES[MODES.length - 1], VisualisationModes.FULL.prev());
        assertEquals(VisualisationModes.FULL, VisualisationModes.NODES.prev());
    }

    @Test
    void nextVisitsEveryModeAndPrevUndoesIt() {
        Set<VisualisationModes> seen = new HashSet<>();
        VisualisationModes mode = VisualisationModes.FULL;
        for (int i = 0; i < MODES.length; i++) {
            seen.add(mode);
            assertEquals(mode, mode.next().prev());
            mode = mode.next();
        }
        assertEquals(VisualisationModes.FULL, mode);
        assertEquals(MODES.length, seen.size());
    }

    @Test
    void byIdMapsValidIds() {
        for (VisualisationModes mode : MODES)
            assertEquals(mode, VisualisationModes.byId(mode.ordinal()));
    }

    @Test
    void byIdFallsBackForBadIds() {
        assertEquals(VisualisationModes.FULL, VisualisationModes.byId(MODES.length));
        assertEquals(VisualisationModes.FULL, VisualisationModes.byId(Integer.MAX_VALUE));
        assertEquals(VisualisationModes.FULL, VisualisationModes.byId(-1));
        assertEquals(VisualisationModes.FULL, VisualisationModes.byId(Integer.MIN_VALUE));
    }
}
