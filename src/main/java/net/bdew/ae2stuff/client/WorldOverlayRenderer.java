package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector3d;

public interface WorldOverlayRenderer {
    /**
     * @param matrices holds the camera rotation
     * @param camera   camera position, subtract it from world coordinates to get render coordinates
     */
    void doRender(MatrixStack matrices, Vector3d camera, float partialTicks);
}
