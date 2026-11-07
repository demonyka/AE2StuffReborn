package net.bdew.ae2stuff.jei;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.bdew.ae2stuff.machines.encoder.ContainerEncoder;
import net.bdew.ae2stuff.network.MsgSetRecipe;
import net.bdew.ae2stuff.network.NetHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;

public class EncoderTransferHandler implements IRecipeTransferHandler<ContainerEncoder> {
    @Override
    public Class<ContainerEncoder> getContainerClass() {
        return ContainerEncoder.class;
    }

    // This form exists in every JEI for 1.16.5, the newer overload with the recipe calls it
    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerEncoder container, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer)
            return null;

        CompoundNBT data = new CompoundNBT();
        int slot = 0;
        for (IGuiIngredient<ItemStack> ingredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (!ingredient.isInput())
                continue;
            ListNBT items = new ListNBT();
            for (ItemStack stack : ingredient.getAllIngredients()) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                items.add(copy.write(new CompoundNBT()));
            }
            data.put(Integer.toString(slot), items);
            slot++;
        }

        NetHandler.sendToServer(new MsgSetRecipe(data));
        return null;
    }
}
