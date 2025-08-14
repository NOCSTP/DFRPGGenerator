package com.dfrpg_gen.game_logic;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.capability.PlayerContributionProvider;
import com.dfrpg_gen.config.ConfigManager;
import com.dfrpg_gen.data.saved_data.HopeWorldData;
import com.dfrpg_gen.event.HopeLevelUpEvent;
import com.dfrpg_gen.networking.ModPackets;
import com.dfrpg_gen.networking.packet.SyncGeneratorDataPacket;
import com.dfrpg_gen.screen.GeneratorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Server-side singleton for managing all contribution logic.
 * This class ensures that all contributions are processed atomically to prevent
 * race conditions and data corruption, fulfilling requirement FR-3.
 */
public final class ContributionManager {

    private static final ContributionManager INSTANCE = new ContributionManager();

    private ContributionManager() {}

    public static ContributionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Processes a player's contribution request. This entire method is synchronized
     * to ensure that only one contribution can be processed at a time across the entire server.
     *
     * @param player   The player making the contribution.
     * @param item     The item being contributed.
     * @param quantity The amount of the item being contributed.
     */
    public synchronized void processContribution(ServerPlayer player, Item item, int quantity) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        HopeWorldData worldData = HopeWorldData.get(server);
        long currentLevelCost = ConfigManager.getLevelCost(worldData.getCurrentLevel());
        int itemValue = ConfigManager.getItemValue(item);

        // --- VALIDATION ---

        // 1. Check if the item is a valid contribution item.
        if (itemValue <= 0) {
            player.sendSystemMessage(Component.translatable("message.dfrpg_gen.contribution.invalid_item"));
            return;
        }

        // 2. Check if the player has enough of the item.
        if (player.getInventory().countItem(item) < quantity) {
            player.sendSystemMessage(Component.translatable("message.dfrpg_gen.contribution.not_enough_items"));
            return;
        }

        long contributionValue = (long) itemValue * quantity;

        // 3. Check the player's personal contribution cap (FR-5).
        player.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
            double capPercentage = ConfigManager.getPlayerContributionCap();
            long personalCap = (long) (currentLevelCost * capPercentage);

            if (cap.getTotalContributionToCurrentLevel() + contributionValue > personalCap) {
                player.sendSystemMessage(Component.translatable("message.dfrpg_gen.contribution.cap_reached"));
                return; // Abort transaction
            }

            // --- ALL VALIDATIONS PASSED: EXECUTE TRANSACTION ---

            // 1. Remove items from player inventory
            player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == item, quantity, player.getInventory());

            // 2. Update player's personal contribution data (Capability)
            cap.addContribution(contributionValue);
            cap.setTrackedLevel(worldData.getCurrentLevel());

            // 3. Update global progress data (SavedData)
            worldData.addProgressPoints(contributionValue);
            worldData.setLastContributionTimestamp(System.currentTimeMillis());

            player.sendSystemMessage(Component.translatable("message.dfrpg_gen.contribution.success", contributionValue));

            // 4. Update GUI for all relevant players in real-time
            updateOpenGuis(server);

            // 5. Check for Level Up
            if (worldData.getCurrentProgressPoints() >= currentLevelCost) {
                handleLevelUp(server, worldData, currentLevelCost);
            }
        });
    }

    /**
     * Sends a data sync packet to all players who currently have the GeneratorMenu open.
     * This ensures the GUI is updated in real-time after a contribution.
     * @param server The MinecraftServer instance.
     */
    private void updateOpenGuis(MinecraftServer server) {
        HopeWorldData worldData = HopeWorldData.get(server);
        long currentProgress = worldData.getCurrentProgressPoints();
        long levelCost = ConfigManager.getLevelCost(worldData.getCurrentLevel());

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.containerMenu instanceof GeneratorMenu) {
                p.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
                    long playerContribution = cap.getTotalContributionToCurrentLevel();
                    ModPackets.sendToPlayer(new SyncGeneratorDataPacket(currentProgress, levelCost, playerContribution), p);
                });
            }
        }
    }

    /**
     * Handles the logic for a Hope Level Up.
     * @param server The server instance.
     * @param worldData The world's persistent data.
     * @param costOfLeveledUpTier The cost of the level that was just completed.
     */
    private void handleLevelUp(MinecraftServer server, HopeWorldData worldData, long costOfLeveledUpTier) {
        long leftoverPoints = worldData.getCurrentProgressPoints() - costOfLeveledUpTier;
        int newLevel = worldData.getCurrentLevel() + 1;

        // Update world data for the new level
        worldData.setCurrentLevel(newLevel);
        worldData.setCurrentProgressPoints(leftoverPoints);

        // ADDED: Log the level up event to the server console/log file.
        LOGGER.info("[{}] Hope Level Up! The server has successfully reached level {}.", DfrpgGen.MOD_ID, newLevel);

        server.getPlayerList().broadcastSystemMessage(Component.translatable("message.dfrpg_gen.levelup.broadcast", newLevel), false);

        // Reset contribution data for all online players
        server.getPlayerList().getPlayers().forEach(p -> {
            p.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
                cap.resetContribution();
            });
        });

        // Fire the event for other systems to listen to (FR-6)
        MinecraftForge.EVENT_BUS.post(new HopeLevelUpEvent(newLevel, server.overworld()));

        // After a level up, the GUI data is now different, so we must update everyone again.
        updateOpenGuis(server);
    }
}