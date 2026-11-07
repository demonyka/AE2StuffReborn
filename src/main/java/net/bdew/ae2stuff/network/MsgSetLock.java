package net.bdew.ae2stuff.network;

import net.bdew.ae2stuff.machines.inscriber.ContainerInscriber;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class MsgSetLock {
    private final String slot;
    private final boolean lock;

    public MsgSetLock(String slot, boolean lock) {
        this.slot = slot;
        this.lock = lock;
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(slot);
        buf.writeBoolean(lock);
    }

    public static MsgSetLock decode(PacketBuffer buf) {
        return new MsgSetLock(buf.readString(16), buf.readBoolean());
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayerEntity player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (player != null && player.openContainer instanceof ContainerInscriber)
                ((ContainerInscriber) player.openContainer).te.setLock(slot, lock);
        });
        return true;
    }
}
