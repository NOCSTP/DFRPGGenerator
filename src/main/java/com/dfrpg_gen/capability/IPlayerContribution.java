package com.dfrpg_gen.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * Defines the contract for the player contribution capability.
 * This capability tracks how much a player has contributed to the current Hope Level,
 * which is essential for enforcing contribution limits (FR-5).
 */
public interface IPlayerContribution {

    /**
     * Gets the total contribution points this player has made to the current level.
     * @return The total contribution points.
     */
    long getTotalContributionToCurrentLevel();

    /**
     * Sets the player's total contribution points for the current level.
     * @param contribution The new total contribution value.
     */
    void setTotalContributionToCurrentLevel(long contribution);

    /**
     * Adds a specified amount to the player's contribution total.
     * @param amount The number of points to add.
     */
    void addContribution(long amount);

    /**
     * Resets the player's contribution for the current level to zero.
     * This should be called when a new Hope Level is reached.
     */
    void resetContribution();

    /**
     * Copies data from another contribution instance.
     * Used for persisting data across player respawns (cloning).
     * @param source The source capability to copy data from.
     */
    void copyFrom(PlayerContribution source);

    /**
     * Saves the capability data to an NBT tag.
     * @param nbt The tag to write data into.
     */
    void saveNBTData(CompoundTag nbt);

    /**
     * Loads the capability data from an NBT tag.
     * @param nbt The tag to read data from.
     */
    void loadNBTData(CompoundTag nbt);

    /**
     * Gets the Hope Level that this contribution data is tracked against.
     * @return The integer level number.
     */
    int getTrackedLevel();

    /**
     * Sets the Hope Level that this contribution data is tracked against.
     * @param level The integer level number.
     */
    void setTrackedLevel(int level);
}