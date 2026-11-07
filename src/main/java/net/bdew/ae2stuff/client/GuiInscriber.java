package net.bdew.ae2stuff.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.bdew.ae2stuff.AE2Stuff;
import net.bdew.ae2stuff.AE2Textures;
import net.bdew.ae2stuff.machines.inscriber.ContainerInscriber;
import net.bdew.ae2stuff.machines.inscriber.TileInscriber;
import net.bdew.ae2stuff.network.MsgSetLock;
import net.bdew.ae2stuff.network.NetHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiInscriber extends BaseScreen<ContainerInscriber> {
    private static final ResourceLocation LOCK_ON = new ResourceLocation(AE2Stuff.MOD_ID, "textures/icons/lock/on.png");
    private static final ResourceLocation LOCK_OFF = new ResourceLocation(AE2Stuff.MOD_ID, "textures/icons/lock/off.png");

    private static final int PROGRESS_X = 135;
    private static final int PROGRESS_Y = 39;
    private static final int PROGRESS_WIDTH = 6;
    private static final int PROGRESS_HEIGHT = 18;
    private static final int PROGRESS_SRC_Y = 177;

    private static final int LOCK_X = 45 - 11;
    private static final int LOCK_TOP_Y = 16 + 4;
    private static final int LOCK_BOTTOM_Y = 62 + 4;
    private static final int LOCK_SIZE = 8;
    private static final int LOCK_TEXTURE_SIZE = 16;

    public GuiInscriber(ContainerInscriber container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title, AE2Textures.INSCRIBER, container.hasToolbox() ? 246 : 211, 176, 176, 176);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrices, partialTicks, mouseX, mouseY);

        TileInscriber te = container.te;
        int fill = Math.round(PROGRESS_HEIGHT * Math.min(te.progress, 1F));
        blit(matrices, guiLeft + PROGRESS_X, guiTop + PROGRESS_Y + PROGRESS_HEIGHT - fill,
                PROGRESS_X, PROGRESS_SRC_Y + PROGRESS_HEIGHT - fill, PROGRESS_WIDTH, fill);

        drawLock(matrices, LOCK_TOP_Y, te.topLocked);
        drawLock(matrices, LOCK_BOTTOM_Y, te.bottomLocked);

        drawUpgradesPanel(matrices, 179, 0, 5);
        if (container.hasToolbox())
            drawToolboxPanel(matrices, 178, 105);
    }

    private void drawLock(MatrixStack matrices, int y, boolean locked) {
        bind(locked ? LOCK_ON : LOCK_OFF);
        AbstractGui.blit(matrices, guiLeft + LOCK_X, guiTop + y, LOCK_SIZE, LOCK_SIZE, 0, 0,
                LOCK_TEXTURE_SIZE, LOCK_TEXTURE_SIZE, LOCK_TEXTURE_SIZE, LOCK_TEXTURE_SIZE);
    }

    @Override
    protected boolean renderCustomTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        TileInscriber te = container.te;
        if (isInRect(mouseX, mouseY, PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT)) {
            tooltip.add(new StringTextComponent(String.format("%.0f%%", te.progress * 100)));
        } else if (isInRect(mouseX, mouseY, LOCK_X, LOCK_TOP_Y, LOCK_SIZE, LOCK_SIZE)) {
            addLockTooltip(tooltip, te.topLocked);
        } else if (isInRect(mouseX, mouseY, LOCK_X, LOCK_BOTTOM_Y, LOCK_SIZE, LOCK_SIZE)) {
            addLockTooltip(tooltip, te.bottomLocked);
        }
        if (tooltip.isEmpty())
            return false;
        func_243308_b(matrices, tooltip, mouseX, mouseY);
        return true;
    }

    private static void addLockTooltip(List<ITextComponent> tooltip, boolean locked) {
        if (locked) {
            tooltip.add(new TranslationTextComponent("ae2stuff.gui.lock.on"));
        } else {
            tooltip.add(new TranslationTextComponent("ae2stuff.gui.lock.off"));
            tooltip.add(new TranslationTextComponent("ae2stuff.gui.lock.note"));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        TileInscriber te = container.te;
        if (isInRect(mouseX, mouseY, LOCK_X, LOCK_TOP_Y, LOCK_SIZE, LOCK_SIZE)) {
            NetHandler.sendToServer(new MsgSetLock("top", !te.topLocked));
            return true;
        }
        if (isInRect(mouseX, mouseY, LOCK_X, LOCK_BOTTOM_Y, LOCK_SIZE, LOCK_SIZE)) {
            NetHandler.sendToServer(new MsgSetLock("bottom", !te.bottomLocked));
            return true;
        }
        return handled;
    }
}
