package com.dfrpg_gen.networking.packet;

import com.dfrpg_gen.screen.GeneratorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C packet to send live updates of contribution data to a player
 * who has the Generator GUI open.
 */
public class SyncGeneratorDataPacket {
    private final long currentProgress;
    private final long levelCost;
    private final long playerContribution;

    public SyncGeneratorDataPacket(long currentProgress, long levelCost, long playerContribution) {
        this.currentProgress = currentProgress;
        this.levelCost = levelCost;
        this.playerContribution = playerContribution;
    }

    public SyncGeneratorDataPacket(FriendlyByteBuf buf) {
        this.currentProgress = buf.readLong();
        this.levelCost = buf.readLong();
        this.playerContribution = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(currentProgress);
        buf.writeLong(levelCost);
        buf.writeLong(playerContribution);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // --- CLIENT-SIDE EXECUTION ---
            // We get the player's currently open menu. If it's our GeneratorMenu, we update its data.
            AbstractContainerMenu openMenu = Minecraft.getInstance().player.containerMenu;
            if(openMenu instanceof GeneratorMenu menu) {
                // This is the FIX: Instead of accessing an invalid method, we use the vanilla
                // AbstractContainerMenu#setData method to update the DataSlots on the client.
                // We split our long values back into two ints, matching the order in the Menu constructor.
                // Indices:
                // 0: currentProgressLow
                // 1: currentProgressHigh
                // 2: levelCostLow
                // 3: levelCostHigh
                // 4: playerContributionLow
                // 5: playerContributionHigh
                menu.setData(0, (int) (this.currentProgress & 0xFFFFFFFFL));
                menu.setData(1, (int) (this.currentProgress >> 32));
                menu.setData(2, (int) (this.levelCost & 0xFFFFFFFFL));
                menu.setData(3, (int) (this.levelCost >> 32));
                menu.setData(4, (int) (this.playerContribution & 0xFFFFFFFFL));
                menu.setData(5, (int) (this.playerContribution >> 32));
            }
        });
        return true;
    }
}