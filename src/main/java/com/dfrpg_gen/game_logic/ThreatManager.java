package com.dfrpg_gen.game_logic;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.config.ConfigManager;
import com.dfrpg_gen.data.saved_data.HopeWorldData;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Server-side singleton for managing the progressive threat system.
 * This class listens for mob spawns and applies attribute modifiers
 * based on the global threat level, fulfilling requirement FR-7.
 */
public final class ThreatManager {

    private static final ThreatManager INSTANCE = new ThreatManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("b9a3a5a2-4a7b-48e2-9b5c-2a623a31b8a1");
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("c3d4a8d4-3e9a-4f2a-b1e8-6d2c8e2b8c9d");
    private static final int PLAYER_SEARCH_RADIUS = 64; // The radius in blocks to search for a nearby player.

    private ThreatManager() {}

    public static ThreatManager getInstance() {
        return INSTANCE;
    }

    /**
     * The core logic method, called when an entity spawns in the world.
     * @param event The entity spawn event.
     */
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        // --- PRE-CHECKS ---
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 1. Check if the entity is a hostile monster.
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();

        // 2. Get currentThreatLevel from HopeWorldData.
        HopeWorldData worldData = HopeWorldData.get(level.getServer());
        int threatLevel = worldData.getCurrentThreatLevel();

        if (threatLevel <= 0) {
            return;
        }

        // --- PROTECTION CHECKS (FR-8) ---

        // 3. Check if the spawn occurs in a safe dimension.
        ResourceLocation dimensionId = level.dimension().location();
        if (ConfigManager.isDimensionSafe(dimensionId)) {
            return;
        }

        // Find the nearest player to apply context-sensitive checks.
        Player nearestPlayer = level.getNearestPlayer(monster, PLAYER_SEARCH_RADIUS);
        if (nearestPlayer == null) {
            // No player nearby, so no threat is applied.
            return;
        }

        // 3a. Check for New Player Protection (placeholder for when a rank/level system is added).
        // if (isNewPlayer(nearestPlayer)) {
        //     return;
        // }

        // --- APPLY MODIFIERS ---

        // 4. If all conditions are met, apply the attribute modifiers.
        applyThreatModifiers(monster, threatLevel);
    }

    /**
     * Applies permanent attribute modifiers to the given entity based on the threat level.
     * @param entity The living entity to modify.
     * @param threatLevel The current global threat level.
     */
    private void applyThreatModifiers(LivingEntity entity, int threatLevel) {
        // The scaling factor is cumulative. At threat level 2, it's 2 * factor.
        double scalingMultiplier = ConfigManager.getThreatScalingFactor() * threatLevel;

        // --- Apply Max Health Modifier ---
        AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        // Check if the modifier is already present to avoid stacking it on the same entity.
        if (healthAttribute != null && healthAttribute.getModifier(HEALTH_MODIFIER_ID) == null) {
            AttributeModifier healthModifier = new AttributeModifier(
                    HEALTH_MODIFIER_ID,
                    "DFRPG-GEN Threat Health Boost",
                    scalingMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE // This is better than MULTIPLY_TOTAL to avoid weird stacking.
            );
            healthAttribute.addPermanentModifier(healthModifier);
            // Heal the mob to its new maximum health.
            entity.heal(Float.MAX_VALUE);
        }

        // --- Apply Attack Damage Modifier ---
        AttributeInstance damageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null && damageAttribute.getModifier(DAMAGE_MODIFIER_ID) == null) {
            AttributeModifier damageModifier = new AttributeModifier(
                    DAMAGE_MODIFIER_ID,
                    "DFRPG-GEN Threat Damage Boost",
                    scalingMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );
            damageAttribute.addPermanentModifier(damageModifier);
        }

        LOGGER.debug("[{}] Applied threat level {} modifiers to {} at {}",
                DfrpgGen.MOD_ID, threatLevel, entity.getType().getDescription().getString(), entity.blockPosition());
    }
}