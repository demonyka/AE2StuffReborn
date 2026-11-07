package net.bdew.ae2stuff.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.bdew.ae2stuff.AE2Stuff;
import net.bdew.ae2stuff.Registration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class AE2StuffJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(AE2Stuff.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (Registration.ENCODER != null)
            registration.addRecipeTransferHandler(new EncoderTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }
}
