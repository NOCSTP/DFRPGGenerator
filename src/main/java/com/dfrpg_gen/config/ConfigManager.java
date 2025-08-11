package com.dfrpg_gen.config;

import com.dfrpg_gen.DfrpgGen;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A helper class to parse and cache configuration values for performant access.
 * This avoids costly string parsing and lookups during frequent operations like contributions or mob spawns.
 * The 'bake' method should be called whenever the server's configuration is loaded or reloaded.
 */
public final class ConfigManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Cached values for quick access. They are immutable to ensure thread safety after baking.
    private static Map<Integer, Long> levelCosts = Collections.emptyMap();
    private static Map<Item, Integer> itemValues = Collections.emptyMap();
    private static Set<ResourceLocation> safeDimensions = Collections.emptySet();

    /**
     * Parses the raw config values from the ForgeConfigSpec (ModConfigs) into optimized, cached maps and sets.
     * This method is the central point for preparing all configuration data for runtime use.
     */
    public static void bake() {
        // --- Parse Level Costs ---
        Map<Integer, Long> bakedLevelCosts = new HashMap<>();
        List<? extends String> rawLevelCosts = ModConfigs.LEVEL_COSTS.get();
        for (String entry : rawLevelCosts) {
            try {
                String[] parts = entry.split(":");
                int level = Integer.parseInt(parts[0]);
                long cost = Long.parseLong(parts[1]);
                bakedLevelCosts.put(level, cost);
            } catch (Exception e) {
                LOGGER.error("[{}] Failed to parse level cost entry: '{}'. Please check the format 'level:cost'.", DfrpgGen.MOD_ID, entry, e);
            }
        }
        levelCosts = Map.copyOf(bakedLevelCosts);
        LOGGER.info("[{}] Baked {} level cost entries from config.", DfrpgGen.MOD_ID, levelCosts.size());

        // --- Parse Item Values ---
        Map<Item, Integer> bakedItemValues = new HashMap<>();
        List<? extends String> rawItemValues = ModConfigs.ITEM_VALUES.get();
        for (String entry : rawItemValues) {
            try {
                String[] parts = entry.split(":");
                ResourceLocation itemRL = new ResourceLocation(parts[0], parts[1]);
                Item item = ForgeRegistries.ITEMS.getValue(itemRL);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    int value = Integer.parseInt(parts[2]);
                    bakedItemValues.put(item, value);
                } else {
                    LOGGER.warn("[{}] Unknown or invalid item specified in item value config: '{}'", DfrpgGen.MOD_ID, itemRL);
                }
            } catch (Exception e) {
                LOGGER.error("[{}] Failed to parse item value entry: '{}'. Please check the format 'modid:item_name:value'.", DfrpgGen.MOD_ID, entry, e);
            }
        }
        itemValues = Map.copyOf(bakedItemValues);
        LOGGER.info("[{}] Baked {} item value entries from config.", DfrpgGen.MOD_ID, itemValues.size());

        // --- Parse Safe Dimensions ---
        Set<ResourceLocation> bakedSafeDimensions = ModConfigs.SAFE_DIMENSIONS.get()
                .stream()
                .map(entry -> {
                    try {
                        return new ResourceLocation(entry);
                    } catch (Exception e) {
                        LOGGER.error("[{}] Failed to parse safe dimension entry: '{}'. Please check the format 'namespace:path'.", DfrpgGen.MOD_ID, entry, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        safeDimensions = Set.copyOf(bakedSafeDimensions);
        LOGGER.info("[{}] Baked {} safe dimension entries from config.", DfrpgGen.MOD_ID, safeDimensions.size());
    }

    /**
     * Gets the configured cost for a specific Hope Level.
     * @param level The level to check.
     * @return The cost in points, or Long.MAX_VALUE if the level is not configured.
     */
    public static long getLevelCost(int level) {
        return levelCosts.getOrDefault(level, Long.MAX_VALUE);
    }

    /**
     * Gets the configured point value for a specific item.
     * @param item The item to check.
     * @return The point value, or 0 if the item is not configured as a valid contribution item.
     */
    public static int getItemValue(Item item) {
        return itemValues.getOrDefault(item, 0);
    }

    /**
     * Gets the configured contribution cap for a single player per level.
     * @return The cap as a decimal percentage (e.g., 0.35 for 35%).
     */
    public static double getPlayerContributionCap() {
        return ModConfigs.PLAYER_CONTRIBUTION_CAP.get();
    }

    /**
     * Gets the configured scaling factor for the threat system.
     * @return The threat scaling multiplier.
     */
    public static double getThreatScalingFactor() {
        return ModConfigs.THREAT_SCALING_FACTOR.get();
    }

    /**
     * Checks if a given dimension is configured as a safe zone where the threat system is disabled.
     * @param dimension The ResourceLocation of the dimension to check.
     * @return True if the dimension is in the safe list, false otherwise.
     */
    public static boolean isDimensionSafe(ResourceLocation dimension) {
        return safeDimensions.contains(dimension);
    }
}