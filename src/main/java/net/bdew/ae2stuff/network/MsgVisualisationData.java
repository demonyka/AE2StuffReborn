package net.bdew.ae2stuff.network;

import net.bdew.ae2stuff.client.VisualiserOverlayRender;
import net.bdew.ae2stuff.items.visualiser.VisualisationData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class MsgVisualisationData {
    private final VisualisationData data;

    public MsgVisualisationData(VisualisationData data) {
        this.data = data;
    }

    public void encode(PacketBuffer buf) {
        data.write(buf);
    }

    public static MsgVisualisationData decode(PacketBuffer buf) {
        return new MsgVisualisationData(VisualisationData.read(buf));
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VisualiserOverlayRender.setData(data)));
        return true;
    }
}
