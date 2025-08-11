package com.dfrpg_gen.event;

import com.dfrpg_gen.DfrpgGen;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * A demonstration implementation of a reward listener.
 * This class listens for the HopeLevelUpEvent and executes simple reward logic,
 * showcasing the decoupled reward system as required by FR-6.
 */
public class RewardListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * This method is triggered whenever the HopeLevelUpEvent is posted on the Forge event bus.
     * @param event The event object containing data about the level up.
     */
    @SubscribeEvent
    public void onHopeLevelUp(HopeLevelUpEvent event) {
        Level level = event.getLevel();
        int newLevel = event.getNewLevel();

        // Ensure we are on the server side before executing server-only logic.
        if (level.isClientSide()) {
            return;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            LOGGER.warn("[{}] Could not execute level up command because server was null.", DfrpgGen.MOD_ID);
            return;
        }

        // The logic to execute as a reward. In this case, a simple /say command.
        // This could be replaced with any other logic, like placing blocks, unlocking recipes, etc.
        String command = "say A new era of hope begins! Level " + newLevel + " has been reached!";
        CommandSourceStack commandSource = server.createCommandSourceStack();

        server.getCommands().performPrefixedCommand(commandSource, command);

        LOGGER.info("[{}] RewardListener triggered for Hope Level {}. Executed command: '{}'", DfrpgGen.MOD_ID, newLevel, command);
    }
}