package com.dfrpg_gen.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Manages all configuration settings for the mod.
 * This class uses Forge's config system to define and register all parameters,
 * which are then loaded from a TOML file. This fulfills FR-9.
 */
public final class ModConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // --- GENERAL SETTINGS ---
    public static final ForgeConfigSpec.DoubleValue PLAYER_CONTRIBUTION_CAP;

    // --- LEVEL SETTINGS ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LEVEL_COSTS;

    // --- CONTRIBUTION SETTINGS ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_VALUES;
    public static final ForgeConfigSpec.IntValue CONTRIBUTION_REPUTATION_REWARD;

    // --- THREAT SETTINGS ---
    public static final ForgeConfigSpec.DoubleValue THREAT_SCALING_FACTOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SAFE_DIMENSIONS;

    static {
        BUILDER.push("dfrpg_gen_config");

        // --- General Category ---
        BUILDER.push("General");
        PLAYER_CONTRIBUTION_CAP = BUILDER
                .comment("Maximum percentage (0.0 to 1.0) a single player can contribute to a level's total cost.",
                        "Default: 0.35 (35%)")
                .defineInRange("playerContributionCap", 0.35, 0.01, 1.0);
        BUILDER.pop();

        // --- Levels Category ---
        BUILDER.push("Levels");
        LEVEL_COSTS = BUILDER
                .comment("Defines the cost in points for each Hope Level.",
                        "Format: \"<level_number>:<cost>\". Example: [\"1:1000\", \"2:5000\"]")
                .defineList("levelCosts", List.of("1:10000", "2:25000", "3:75000"),
                        (obj) -> obj instanceof String && ((String) obj).matches("\\d+:\\d+"));
        BUILDER.pop();

        // --- Contributions Category ---
        BUILDER.push("Contributions");
        ITEM_VALUES = BUILDER
                .comment("Defines the point value for each contribution item.",
                        "Format: \"<modid>:<item_name>:<value>\". Example: [\"minecraft:diamond:100\", \"minecraft:iron_ingot:10\"]")
                .defineList("itemValues", List.of("minecraft:diamond:100", "minecraft:gold_ingot:50", "minecraft:iron_ingot:10"),
                        (obj) -> obj instanceof String && ((String) obj).matches("[a-z_]+:[a-z_]+:\\d+"));

        CONTRIBUTION_REPUTATION_REWARD = BUILDER
                .comment("The amount of personal reputation/reward points a player receives per contribution action.")
                .defineInRange("contributionReputationReward", 1, 0, Integer.MAX_VALUE);
        BUILDER.pop();


        // --- Threat System Category ---
        BUILDER.push("Threat");
        THREAT_SCALING_FACTOR = BUILDER
                .comment("A multiplier applied to mob attributes based on the current threat level.")
                .defineInRange("threatScalingFactor", 1.1, 1.0, 100.0);

        SAFE_DIMENSIONS = BUILDER
                .comment("A list of dimension resource locations where the threat system will not apply.",
                        "Example: [\"minecraft:the_end\", \"some_mod:safe_dimension\"]")
                .defineList("safeDimensions", List.of("minecraft:overworld"),
                        (obj) -> obj instanceof String && ((String) obj).matches("[a-z_]+:[a-z_]+"));
        BUILDER.pop();


        BUILDER.pop(); // Pop dfrpg_gen_config
        SPEC = BUILDER.build();
    }
}