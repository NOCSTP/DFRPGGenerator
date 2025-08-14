package com.dfrpg_gen.event;

import com.dfrpg_gen.capability.PlayerContributionProvider;
import com.dfrpg_gen.data.saved_data.HopeWorldData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerLoginHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            HopeWorldData worldData = HopeWorldData.get(player.getServer());
            int serverLevel = worldData.getCurrentLevel();

            player.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
                if (cap.getTrackedLevel() != serverLevel) {
                    cap.resetContribution();
                    cap.setTrackedLevel(serverLevel);
                }
            });
        }
    }
}
