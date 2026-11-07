package net.bdew.ae2stuff.client;

import net.bdew.ae2stuff.AE2Stuff;
import net.bdew.ae2stuff.machines.encoder.ContainerEncoder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiEncoder extends BaseScreen<ContainerEncoder> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(AE2Stuff.MOD_ID, "textures/gui/encoder.png");

    public GuiEncoder(ContainerEncoder container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title, BACKGROUND, 176, 166, 176, 166);
    }
}
