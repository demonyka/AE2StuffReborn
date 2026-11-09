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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        List<List<ItemStack>> slots = new ArrayList<>();
        // The grid position comes from the slot index, the map gives no order
        Map<Integer, ? extends IGuiIngredient<ItemStack>> bySlot = new TreeMap<>(recipeLayout.getItemStacks().getGuiIngredients());
        for (IGuiIngredient<ItemStack> ingredient : bySlot.values()) {
            if (!ingredient.isInput())
                continue;
            List<ItemStack> options = new ArrayList<>();
            for (ItemStack stack : ingredient.getAllIngredients()) {
                if (stack == null || stack.isEmpty())
                    continue;
                ItemStack copy = stack.copy();
                copy.setCount(1);
                options.add(copy);
            }
            slots.add(options);
        }

        NetHandler.sendToServer(MsgSetRecipe.fit(slots));
        return null;
    }
}
