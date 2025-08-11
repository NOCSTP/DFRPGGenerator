package com.dfrpg_gen.networking;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.networking.packet.ContributeItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Manages the registration and sending of all network packets for the mod.
 */
public class ModPackets {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                // FIX: Use the new helper method.
                .named(DfrpgGen.asResource("messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Register all packets here
        net.messageBuilder(ContributeItemPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ContributeItemPacket::toBytes)
                .decoder(ContributeItemPacket::new)
                .consumerMainThread(ContributeItemPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}