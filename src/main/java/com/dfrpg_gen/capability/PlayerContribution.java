package com.dfrpg_gen.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * The default implementation of the IPlayerContribution capability.
 * It stores the player's contribution data and handles its serialization.
 */
public class PlayerContribution implements IPlayerContribution {

    private long totalContributionToCurrentLevel = 0;
    private final String NBT_KEY = "totalContributionToCurrentLevel";

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
    }

    @Override
    public void saveNBTData(CompoundTag nbt) {
        nbt.putLong(NBT_KEY, totalContributionToCurrentLevel);
    }

    @Override
    public void loadNBTData(CompoundTag nbt) {
        this.totalContributionToCurrentLevel = nbt.getLong(NBT_KEY);
    }
}