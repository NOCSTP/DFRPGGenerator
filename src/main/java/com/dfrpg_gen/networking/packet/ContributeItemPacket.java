package com.dfrpg_gen.networking.packet;

import com.dfrpg_gen.game_logic.ContributionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * A C2S (Client-to-Server) packet sent when a player attempts to contribute an item.
 */
public class ContributeItemPacket {

    private final ResourceLocation itemRegistryName;
    private final int count;

    public ContributeItemPacket(Item item, int count) {
        this.itemRegistryName = ForgeRegistries.ITEMS.getKey(item);
        this.count = count;
    }

    public ContributeItemPacket(FriendlyByteBuf buf) {
        this.itemRegistryName = buf.readResourceLocation();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(itemRegistryName);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Server-side execution
            ServerPlayer player = context.getSender();
            Item item = ForgeRegistries.ITEMS.getValue(this.itemRegistryName);

            if (player != null && item != null) {
                ContributionManager.getInstance().processContribution(player, item, this.count);
            }
        });
        return true;
    }
}