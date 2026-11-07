package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.bdew.ae2stuff.AE2Stuff;
import net.bdew.ae2stuff.machines.grower.ContainerGrower;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGrower extends BaseScreen<ContainerGrower> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(AE2Stuff.MOD_ID, "textures/gui/grower.png");

    public GuiGrower(ContainerGrower container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title, BACKGROUND, container.hasToolbox() ? 246 : 211, 166, 176, 166);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrices, partialTicks, mouseX, mouseY);
        drawUpgradesPanel(matrices, 179, 0, 3);
        if (container.hasToolbox())
            drawToolboxPanel(matrices, 178, 86);
    }
}
