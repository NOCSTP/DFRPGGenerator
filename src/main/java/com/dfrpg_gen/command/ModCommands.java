package com.dfrpg_gen.command;

import com.dfrpg_gen.capability.PlayerContributionProvider;
import com.dfrpg_gen.data.saved_data.HopeWorldData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles the registration and execution of all mod-specific commands.
 */
public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main command literal: /dfrpg
        LiteralArgumentBuilder<CommandSourceStack> dfrpgCommand = Commands.literal("dfrpg")
                .then(registerAdminCommands());

        dispatcher.register(dfrpgCommand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerAdminCommands() {
        // Subcommand: /dfrpg admin ...
        return Commands.literal("admin")
                // Require permission level 2 (default for most admin commands like /gamemode, /kick)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.literal("all")
                                .executes(context -> {
                                    MinecraftServer server = context.getSource().getServer();
                                    // Reset global data
                                    HopeWorldData worldData = HopeWorldData.get(server);
                                    worldData.setCurrentLevel(1);
                                    worldData.setCurrentProgressPoints(0);
                                    worldData.setCurrentThreatLevel(0);
                                    worldData.setDirty(); // Ensure data is saved

                                    // Reset contribution data for all online players
                                    for(ServerPlayer player : server.getPlayerList().getPlayers()) {
                                        player.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
                                            cap.resetContribution();
                                        });
                                    }

                                    // Notify players and command sender
                                    context.getSource().sendSuccess(() -> Component.literal("Server progression has been fully reset."), true);
                                    server.getPlayerList().broadcastSystemMessage(
                                            Component.translatable("message.dfrpg_gen.command.reset_broadcast"), false);

                                    return 1; // Success
                                })
                        )
                );
    }
}