package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bdew.ae2stuff.AE2Textures;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseScreen<T extends Container> extends ContainerScreen<T> {
    private static final int PANEL_TEXTURE_SIZE = 128;
    private static final int PANEL_WIDTH = 32;
    private static final int PANEL_PADDING = 7;
    private static final int SLOT_SIZE = 18;
    private static final int TOOLBOX_SRC = 60;
    private static final int TOOLBOX_SIZE = 68;

    private final ResourceLocation background;
    private final int backgroundWidth;
    private final int backgroundHeight;

    protected BaseScreen(T container, PlayerInventory inventory, ITextComponent title, ResourceLocation background,
                         int width, int height, int backgroundWidth, int backgroundHeight) {
        super(container, inventory, title);
        this.background = background;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        xSize = width;
        ySize = height;
        playerInventoryTitleY = height - 96 + 3;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
        if (!renderCustomTooltip(matrices, mouseX, mouseY))
            renderHoveredTooltip(matrices, mouseX, mouseY);
    }

    /**
     * @return true if a tooltip was rendered, then the slot tooltip is skipped
     */
    protected boolean renderCustomTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        return false;
    }

    protected boolean isInRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        double localX = mouseX - guiLeft;
        double localY = mouseY - guiTop;
        return localX >= x && localX < x + width && localY >= y && localY < y + height;
    }

    protected void bind(ResourceLocation texture) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(texture);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        bind(background);
        blit(matrices, guiLeft, guiTop, 0, 0, backgroundWidth, backgroundHeight);
    }

    /**
     * Draws a single column upgrades panel the same way as AE2's UpgradesPanel.
     */
    protected void drawUpgradesPanel(MatrixStack matrices, int x, int y, int slots) {
        bind(AE2Textures.EXTRA_PANELS);
        for (int i = 0; i < slots; i++) {
            int destY = guiTop + y + PANEL_PADDING + i * SLOT_SIZE;
            int srcY = PANEL_PADDING;
            int height = SLOT_SIZE;
            if (i == 0) {
                destY -= PANEL_PADDING;
                srcY = 0;
                height += PANEL_PADDING;
            }
            if (i == slots - 1)
                height += PANEL_PADDING;
            blit(matrices, guiLeft + x, destY, 0, srcY, PANEL_WIDTH, height, PANEL_TEXTURE_SIZE, PANEL_TEXTURE_SIZE);
        }
    }

    protected void drawToolboxPanel(MatrixStack matrices, int x, int y) {
        bind(AE2Textures.EXTRA_PANELS);
        blit(matrices, guiLeft + x, guiTop + y, TOOLBOX_SRC, TOOLBOX_SRC, TOOLBOX_SIZE, TOOLBOX_SIZE, PANEL_TEXTURE_SIZE, PANEL_TEXTURE_SIZE);
    }
}
