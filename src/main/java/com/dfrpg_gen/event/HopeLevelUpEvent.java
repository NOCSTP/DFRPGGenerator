package com.dfrpg_gen.event;

import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the server-side MinecraftForge.EVENT_BUS when the global Hope Level increases.
 * <p>
 * This event is the core component for the "Event-Driven Rewards" requirement (FR-6).
 * It allows various parts of the mod, or even external mods, to listen for progression
 * milestones and trigger their own logic (e.g., unlocking content, spawning NPCs,
 * removing barriers) without being directly coupled to the contribution system.
 *
 * @see net.minecraftforge.common.MinecraftForge#EVENT_BUS
 */
public class HopeLevelUpEvent extends Event {

    private final int newLevel;
    private final Level level;

    /**
     * Constructs a new instance of the HopeLevelUpEvent.
     * This should only be fired from the server-side logic when a level-up condition is met.
     *
     * @param newLevel The new Hope Level that has just been reached.
     * @param level    The server world instance where the event occurred.
     */
    public HopeLevelUpEvent(int newLevel, Level level) {
        this.newLevel = newLevel;
        this.level = level;
    }

    /**
     * Returns the new Hope Level that was reached.
     *
     * @return The integer value of the new level.
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * Returns the Level (world) in which the progression occurred.
     *
     * @return The Level instance.
     */
    public Level getLevel() {
        return level;
    }
}