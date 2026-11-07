package net.bdew.ae2stuff.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.bdew.ae2stuff.machines.encoder.ContainerEncoder;
import net.bdew.ae2stuff.machines.encoder.TileEncoder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sets the encoder recipe from JEI. Each recipe slot gets the list of stacks accepted in that slot.
 */
public final class MsgSetRecipe {
    // Packets from the client above 32767 bytes disconnect the player, this leaves room for the framing
    private static final int MAX_PAYLOAD = 30000;
    private static final int MAX_OPTIONS = 256;

    private final List<List<ItemStack>> slots;

    private MsgSetRecipe(List<List<ItemStack>> slots) {
        this.slots = slots;
    }

    /**
     * Builds the message, dropping alternatives from the longest lists until it fits in one packet.
     */
    public static MsgSetRecipe fit(List<List<ItemStack>> options) {
        List<List<ItemStack>> slots = new ArrayList<>();
        for (int i = 0; i < Math.min(options.size(), TileEncoder.SLOT_RECIPE_COUNT); i++) {
            List<ItemStack> slot = options.get(i);
            slots.add(new ArrayList<>(slot.subList(0, Math.min(slot.size(), MAX_OPTIONS))));
        }
        MsgSetRecipe message = new MsgSetRecipe(slots);
        while (message.size() > MAX_PAYLOAD) {
            List<ItemStack> longest = null;
            for (List<ItemStack> slot : slots) {
                if (slot.size() > 1 && (longest == null || slot.size() > longest.size()))
                    longest = slot;
            }
            if (longest == null)
                break;
            longest.subList(Math.max(1, longest.size() * 3 / 4), longest.size()).clear();
        }
        return message;
    }

    private int size() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        encode(buf);
        int size = buf.writerIndex();
        buf.release();
        return size;
    }

    public void encode(PacketBuffer buf) {
        buf.writeVarInt(slots.size());
        for (List<ItemStack> slot : slots) {
            buf.writeVarInt(slot.size());
            for (ItemStack stack : slot)
                buf.writeItemStack(stack, false);
        }
    }

    public static MsgSetRecipe decode(PacketBuffer buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > TileEncoder.SLOT_RECIPE_COUNT)
            throw new DecoderException("Bad recipe slot count " + count);
        List<List<ItemStack>> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int options = buf.readVarInt();
            if (options < 0 || options > MAX_OPTIONS)
                throw new DecoderException("Bad option count " + options);
            List<ItemStack> slot = new ArrayList<>(options);
            for (int j = 0; j < options; j++)
                slot.add(buf.readItemStack());
            slots.add(slot);
        }
        return new MsgSetRecipe(slots);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayerEntity player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (player != null && player.openContainer instanceof ContainerEncoder && player.openContainer.canInteractWith(player))
                ((ContainerEncoder) player.openContainer).te.setRecipeFromNetwork(slots);
        });
        return true;
    }
}
