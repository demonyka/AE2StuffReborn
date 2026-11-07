package net.bdew.ae2stuff.network;

import net.bdew.ae2stuff.machines.encoder.ContainerEncoder;
import net.bdew.ae2stuff.machines.encoder.TileEncoder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sets the encoder recipe from JEI. Each slot number maps to a list of the stacks accepted in that slot.
 */
public final class MsgSetRecipe {
    private final CompoundNBT recipe;

    public MsgSetRecipe(CompoundNBT recipe) {
        this.recipe = recipe;
    }

    public void encode(PacketBuffer buf) {
        buf.writeCompoundTag(recipe);
    }

    public static MsgSetRecipe decode(PacketBuffer buf) {
        return new MsgSetRecipe(buf.readCompoundTag());
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayerEntity player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (player != null && player.openContainer instanceof ContainerEncoder)
                apply((ContainerEncoder) player.openContainer);
        });
        return true;
    }

    private void apply(ContainerEncoder container) {
        for (int slot = 0; slot < TileEncoder.SLOT_RECIPE_COUNT; slot++) {
            ListNBT list = recipe.getList(Integer.toString(slot), Constants.NBT.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++)
                items.add(ItemStack.read(list.getCompound(i)));
            container.te.setInventorySlotContents(slot, items.isEmpty() ? ItemStack.EMPTY : container.te.findMatchingRecipeStack(items));
        }
        container.updateRecipe();
    }
}
