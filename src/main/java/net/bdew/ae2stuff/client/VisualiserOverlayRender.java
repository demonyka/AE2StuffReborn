package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.items.visualiser.ItemVisualiser;
import net.bdew.ae2stuff.items.visualiser.VisualisationData;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VLink;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VNode;
import net.bdew.ae2stuff.items.visualiser.VisualisationModes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class VisualiserOverlayRender implements WorldOverlayRenderer {
    private static final double SIZE = 0.2D;
    private static final double LABEL_DISTANCE = 64D;

    private static final Set<VisualisationModes> RENDER_NODES_MODES =
            EnumSet.of(VisualisationModes.NODES, VisualisationModes.FULL, VisualisationModes.NONUM);
    private static final Set<VisualisationModes> RENDER_LINKS_MODES =
            EnumSet.of(VisualisationModes.CHANNELS, VisualisationModes.FULL, VisualisationModes.NONUM, VisualisationModes.P2P);

    private static VisualisationData currentLinks = new VisualisationData();
    private static List<VLink> dense = new ArrayList<>();
    private static List<VLink> normal = new ArrayList<>();

    public static void setData(VisualisationData data) {
        List<VLink> newDense = new ArrayList<>();
        List<VLink> newNormal = new ArrayList<>();
        for (VLink link : data.links)
            (link.has(VisualisationData.LINK_DENSE) ? newDense : newNormal).add(link);
        currentLinks = data;
        dense = newDense;
        normal = newNormal;
    }

    // The server only sends data while a visualiser is held, so data from another world must not linger
    @SubscribeEvent
    public static void handleLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        setData(new VisualisationData());
    }

    // Fired on dimension changes too
    @SubscribeEvent
    public static void handleRespawn(ClientPlayerNetworkEvent.RespawnEvent event) {
        setData(new VisualisationData());
    }

    private static final class Quads {
        final BufferBuilder buffer;
        final Matrix4f matrix;
        final Vector3d camera;
        float r;
        float g;
        float b;
        double x;
        double y;
        double z;

        Quads(BufferBuilder buffer, Matrix4f matrix, Vector3d camera) {
            this.buffer = buffer;
            this.matrix = matrix;
            this.camera = camera;
        }

        void color(float red, float green, float blue, float multiplier) {
            r = red * multiplier;
            g = green * multiplier;
            b = blue * multiplier;
        }

        void corner(double dx, double dy, double dz) {
            buffer.pos(matrix, (float) (x + dx - camera.x), (float) (y + dy - camera.y), (float) (z + dz - camera.z))
                    .color(r, g, b, 1F).endVertex();
        }
    }

    private static void renderNodes(Matrix4f matrix, Vector3d camera) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Quads q = new Quads(buffer, matrix, camera);

        for (VNode node : currentLinks.nodes) {
            float r = 0F;
            float g = 0F;
            float b = 1F;
            if (node.has(VisualisationData.NODE_MISSING)) {
                r = 1F;
                b = 0F;
            } else if (node.has(VisualisationData.NODE_DENSE)) {
                r = 1F;
                g = 1F;
                b = 0F;
            }

            q.x = node.x + 0.5D;
            q.y = node.y + 0.5D;
            q.z = node.z + 0.5D;

            q.color(r, g, b, 1F); // +Y
            q.corner(-SIZE, SIZE, SIZE);
            q.corner(SIZE, SIZE, SIZE);
            q.corner(SIZE, SIZE, -SIZE);
            q.corner(-SIZE, SIZE, -SIZE);

            q.color(r, g, b, 0.5F); // -Y
            q.corner(SIZE, -SIZE, -SIZE);
            q.corner(SIZE, -SIZE, SIZE);
            q.corner(-SIZE, -SIZE, SIZE);
            q.corner(-SIZE, -SIZE, -SIZE);

            q.color(r, g, b, 0.8F); // +/- Z
            q.corner(SIZE, -SIZE, SIZE);
            q.corner(SIZE, SIZE, SIZE);
            q.corner(-SIZE, SIZE, SIZE);
            q.corner(-SIZE, -SIZE, SIZE);

            q.corner(-SIZE, SIZE, -SIZE);
            q.corner(SIZE, SIZE, -SIZE);
            q.corner(SIZE, -SIZE, -SIZE);
            q.corner(-SIZE, -SIZE, -SIZE);

            q.color(r, g, b, 0.6F); // +/- X
            q.corner(SIZE, SIZE, -SIZE);
            q.corner(SIZE, SIZE, SIZE);
            q.corner(SIZE, -SIZE, SIZE);
            q.corner(SIZE, -SIZE, -SIZE);

            q.corner(-SIZE, -SIZE, SIZE);
            q.corner(-SIZE, SIZE, SIZE);
            q.corner(-SIZE, SIZE, -SIZE);
            q.corner(-SIZE, -SIZE, -SIZE);
        }

        Tessellator.getInstance().draw();
    }

    private static void renderLinks(List<VLink> links, float width, boolean onlyP2P, Matrix4f matrix, Vector3d camera) {
        RenderSystem.lineWidth(width);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        Quads q = new Quads(buffer, matrix, camera);

        for (VLink link : links) {
            boolean compressed = link.has(VisualisationData.LINK_COMPRESSED);
            if (onlyP2P && !compressed)
                continue;
            if (compressed)
                q.color(1F, 0F, 1F, 1F);
            else if (link.has(VisualisationData.LINK_DENSE))
                q.color(1F, 1F, 0F, 1F);
            else
                q.color(0F, 0F, 1F, 1F);

            q.x = link.node1.x;
            q.y = link.node1.y;
            q.z = link.node1.z;
            q.corner(0.5D, 0.5D, 0.5D);
            q.x = link.node2.x;
            q.y = link.node2.y;
            q.z = link.node2.z;
            q.corner(0.5D, 0.5D, 0.5D);
        }

        Tessellator.getInstance().draw();
    }

    @Override
    public void doRender(MatrixStack matrices, Vector3d camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ItemVisualiser visualiser = Registration.VISUALISER.get();
        ItemStack stack = visualiser.getHeldStack(mc.player);
        if (stack.isEmpty())
            return;

        VisualisationModes mode = visualiser.getMode(stack);
        Matrix4f matrix = matrices.getLast().getMatrix();

        OverlayRenderHandler.beginOverlay();
        if (RENDER_NODES_MODES.contains(mode))
            renderNodes(matrix, camera);
        if (RENDER_LINKS_MODES.contains(mode)) {
            renderLinks(dense, 16F, mode == VisualisationModes.P2P, matrix, camera);
            renderLinks(normal, 4F, mode == VisualisationModes.P2P, matrix, camera);
        }
        OverlayRenderHandler.endOverlay();

        if (mode == VisualisationModes.FULL) {
            IRenderTypeBuffer.Impl buffer = OverlayRenderHandler.textBuffer();
            for (VLink link : currentLinks.links) {
                if (link.channels <= 0)
                    continue;
                double x = (link.node1.x + link.node2.x) / 2D + 0.5D - camera.x;
                double y = (link.node1.y + link.node2.y) / 2D + 0.5D - camera.y;
                double z = (link.node1.z + link.node2.z) / 2D + 0.5D - camera.z;
                if (x * x + y * y + z * z > LABEL_DISTANCE * LABEL_DISTANCE)
                    continue;
                OverlayRenderHandler.renderFloatingText(matrices, buffer, Integer.toString(link.channels), x, y, z, 0xFFFFFF);
            }
            buffer.finish();
        }
    }
}
