package net.bdew.ae2stuff.network;

import net.bdew.ae2stuff.AE2Stuff;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class NetHandler {
    private static final String PROTOCOL = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AE2Stuff.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private NetHandler() {
    }

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(MsgSetRecipe.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MsgSetRecipe::encode).decoder(MsgSetRecipe::decode).consumer(MsgSetRecipe::handle).add();
        CHANNEL.messageBuilder(MsgSetLock.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MsgSetLock::encode).decoder(MsgSetLock::decode).consumer(MsgSetLock::handle).add();
        CHANNEL.messageBuilder(MsgVisualisationData.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MsgVisualisationData::encode).decoder(MsgVisualisationData::decode).consumer(MsgVisualisationData::handle).add();
        CHANNEL.messageBuilder(MsgVisualisationMode.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MsgVisualisationMode::encode).decoder(MsgVisualisationMode::decode).consumer(MsgVisualisationMode::handle).add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendTo(Object message, ServerPlayerEntity player) {
        CHANNEL.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }
}
