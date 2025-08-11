package com.dfrpg_gen.event;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.capability.PlayerContributionProvider;
import com.dfrpg_gen.command.ModCommands;
import com.dfrpg_gen.game_logic.ThreatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles mod-specific server-side events, such as attaching capabilities.
 */
@Mod.EventBusSubscriber(modid = DfrpgGen.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).isPresent()) {
                // FIX: Use the new helper method.
                event.addCapability(DfrpgGen.asResource("player_contribution"),
                        new PlayerContributionProvider());
            }
        }
    }

    /**
     * Copies the player's contribution data when they are cloned.
     * This is typically used to persist data after death.
     * @param event The player clone event.
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // Copy data from the original player to the new one.
            event.getOriginal().getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(newStore -> {
                    newStore.copyFrom((com.dfrpg_gen.capability.PlayerContribution) oldStore);
                });
            });
        }
    }
    /**
     * Listens for any entity spawning in the world.
     * Delegates the logic to the ThreatManager to keep this class clean.
     * @param event The entity spawn event.
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        ThreatManager.getInstance().onEntitySpawn(event);
    }

    /**
     * Registers all server-side commands when the server is starting.
     * @param event The command registration event.
     */
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}