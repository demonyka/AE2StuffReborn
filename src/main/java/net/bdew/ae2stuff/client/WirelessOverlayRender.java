package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

public final class WirelessOverlayRender implements WorldOverlayRenderer {
    @Override
    public void doRender(MatrixStack matrices, Vector3d camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        RayTraceResult hit = mc.objectMouseOver;
        if (hit == null || hit.getType() != RayTraceResult.Type.BLOCK || mc.world == null)
            return;

        BlockPos pos = ((BlockRayTraceResult) hit).getPos();
        TileEntity tile = mc.world.getTileEntity(pos);
        if (!(tile instanceof TileWireless))
            return;
        BlockPos other = ((TileWireless) tile).getLinkPos();
        if (other == null)
            return;

        OverlayRenderHandler.beginOverlay();
        RenderSystem.lineWidth(4F);

        Matrix4f matrix = matrices.getLast().getMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        vertex(buffer, matrix, pos, camera);
        vertex(buffer, matrix, other, camera);
        Tessellator.getInstance().draw();

        OverlayRenderHandler.endOverlay();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vector3d camera) {
        buffer.pos(matrix, (float) (pos.getX() + 0.5D - camera.x), (float) (pos.getY() + 0.5D - camera.y), (float) (pos.getZ() + 0.5D - camera.z))
                .color(0F, 0F, 1F, 1F).endVertex();
    }
}
