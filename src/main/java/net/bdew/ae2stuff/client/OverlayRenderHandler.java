package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bdew.ae2stuff.AE2Stuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public final class OverlayRenderHandler {
    private static final List<WorldOverlayRenderer> RENDERERS = new ArrayList<>();

    private static final float TEXT_SCALE = 0.027F;
    private static final int TEXT_Y_OFFSET = -4;
    private static final int TEXT_BACKGROUND = 0x80000000;
    private static final int FULL_BRIGHT = 0xF000F0;

    private static boolean fogWasEnabled;

    private OverlayRenderHandler() {
    }

    static void register(WorldOverlayRenderer renderer) {
        RENDERERS.add(0, renderer);
    }

    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        Vector3d camera = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        for (WorldOverlayRenderer renderer : RENDERERS) {
            try {
                renderer.doRender(event.getMatrixStack(), camera, event.getPartialTicks());
            } catch (RuntimeException e) {
                AE2Stuff.LOG.error("Error in overlay renderer {}", renderer, e);
            }
        }
    }

    /**
     * Sets up the state for flat colored geometry that is visible through blocks.
     */
    @SuppressWarnings("deprecation")
    static void beginOverlay() {
        fogWasEnabled = GL11.glIsEnabled(GL11.GL_FOG);
        RenderSystem.disableLighting();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableFog();
        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    @SuppressWarnings("deprecation")
    static void endOverlay() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
        if (fogWasEnabled)
            RenderSystem.enableFog();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
    }

    /**
     * Buffer for {@link #renderFloatingText}, the caller draws all queued text with one finish()
     */
    static IRenderTypeBuffer.Impl textBuffer() {
        return IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
    }

    static void renderFloatingText(MatrixStack matrices, IRenderTypeBuffer buffer, String text, double x, double y, double z, int color) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.fontRenderer;

        matrices.push();
        matrices.translate(x, y, z);
        matrices.rotate(mc.getRenderManager().getCameraOrientation());
        matrices.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        font.renderString(text, -font.getStringWidth(text) / 2F, TEXT_Y_OFFSET, color, false,
                matrices.getLast().getMatrix(), buffer, true, TEXT_BACKGROUND, FULL_BRIGHT);

        matrices.pop();
    }
}
