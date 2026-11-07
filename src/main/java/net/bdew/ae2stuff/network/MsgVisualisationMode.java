package net.bdew.ae2stuff.network;

import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.items.visualiser.VisualisationModes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class MsgVisualisationMode {
    private final VisualisationModes mode;

    public MsgVisualisationMode(VisualisationModes mode) {
        this.mode = mode;
    }

    public void encode(PacketBuffer buf) {
        buf.writeByte(mode.ordinal());
    }

    public static MsgVisualisationMode decode(PacketBuffer buf) {
        return new MsgVisualisationMode(VisualisationModes.byId(buf.readByte()));
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayerEntity player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (player != null)
                Registration.VISUALISER.get().handleModeChange(player, mode);
        });
        return true;
    }
}
