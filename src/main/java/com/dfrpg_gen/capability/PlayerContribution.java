package com.dfrpg_gen.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * The default implementation of the IPlayerContribution capability.
 * It stores the player's contribution data and handles its serialization.
 */
public class PlayerContribution implements IPlayerContribution {

    private long totalContributionToCurrentLevel = 0;
    private int trackedLevel = 1; // Default to 1, as players start at level 1
    private final String NBT_KEY_CONTRIBUTION = "totalContributionToCurrentLevel";
    private final String NBT_KEY_LEVEL = "trackedLevel";

    @Override
    public long getTotalContributionToCurrentLevel() {
        return this.totalContributionToCurrentLevel;
    }

    @Override
    public void setTotalContributionToCurrentLevel(long contribution) {
        this.totalContributionToCurrentLevel = contribution;
    }

    @Override
    public void addContribution(long amount) {
        this.totalContributionToCurrentLevel += amount;
    }

    @Override
    public void resetContribution() {
        this.totalContributionToCurrentLevel = 0;
    }

    @Override
    public void copyFrom(PlayerContribution source) {
        this.totalContributionToCurrentLevel = source.totalContributionToCurrentLevel;
        this.trackedLevel = source.trackedLevel;
    }

    @Override
    public void saveNBTData(CompoundTag nbt) {
        nbt.putLong(NBT_KEY_CONTRIBUTION, totalContributionToCurrentLevel);
        nbt.putInt(NBT_KEY_LEVEL, trackedLevel);
    }

    @Override
    public void loadNBTData(CompoundTag nbt) {
        this.totalContributionToCurrentLevel = nbt.getLong(NBT_KEY_CONTRIBUTION);
        if (nbt.contains(NBT_KEY_LEVEL)) {
            this.trackedLevel = nbt.getInt(NBT_KEY_LEVEL);
        }
    }

    @Override
    public int getTrackedLevel() {
        return this.trackedLevel;
    }

    @Override
    public void setTrackedLevel(int level) {
        this.trackedLevel = level;
    }
}