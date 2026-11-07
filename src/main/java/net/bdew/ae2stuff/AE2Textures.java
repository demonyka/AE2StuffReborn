package net.bdew.ae2stuff;

import net.minecraft.util.ResourceLocation;

/**
 * Textures that are read from AE2 at runtime.
 * AE2 8.x moved the upgrade and toolbox panels out of the per-machine GUI textures into extra_panels.png.
 */
public final class AE2Textures {
    public static final ResourceLocation INSCRIBER = new ResourceLocation("appliedenergistics2", "textures/guis/inscriber.png");
    public static final ResourceLocation EXTRA_PANELS = new ResourceLocation("appliedenergistics2", "textures/guis/extra_panels.png");

    private AE2Textures() {
    }
}
