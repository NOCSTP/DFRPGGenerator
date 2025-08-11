package com.dfrpg_gen.data.saved_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Stores all global, persistent data for the mod on a per-world basis.
 * This class handles all data related to the server's overall progression,
 * such as Hope Level, progress points, and threat level. It is stored in the
 * overworld's data folder to ensure it is always available on the server.
 * This directly implements the requirements for Component 2a.
 */
public class HopeWorldData extends SavedData {

    private static final String DATA_NAME = "dfrpg_gen_hope_world_data";

    // Data fields with default values
    private int currentLevel = 1;
    private long currentProgressPoints = 0L;
    private long lastContributionTimestamp = 0L;
    private int currentThreatLevel = 0;

    /**
     * Private constructor used by the factory methods.
     */
    public HopeWorldData() {}

    /**
     * Factory method to get the HopeWorldData instance for the server.
     * It safely retrieves the existing data storage or creates a new one if it doesn't exist. [2]
     * The data is attached to the Overworld to ensure it is global and persistent. [2]
     *
     * @param server The MinecraftServer instance.
     * @return The singleton instance of HopeWorldData for the server.
     */
    public static HopeWorldData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        // This is a null-safe check, though the overworld should never be null on a running server.
        if (overworld == null) {
            throw new IllegalStateException("Cannot get HopeWorldData because the Overworld is not loaded!");
        }

        DimensionDataStorage storage = overworld.getDataStorage();
        // computeIfAbsent is the modern, safe way to load or create SavedData. [2, 8]
        // It takes a load function, a creation function (supplier), and the data name.
        return storage.computeIfAbsent(HopeWorldData::load, HopeWorldData::new, DATA_NAME);
    }

    /**
     * Loads the data from an NBT compound tag.
     * This method is called by the game when loading the world data from disk.
     *
     * @param nbt The CompoundTag containing the saved data.
     * @return A new HopeWorldData instance populated with data from the NBT tag.
     */
    public static HopeWorldData load(CompoundTag nbt) {
        HopeWorldData data = new HopeWorldData();
        data.currentLevel = nbt.getInt("currentLevel");
        data.currentProgressPoints = nbt.getLong("currentProgressPoints");
        data.lastContributionTimestamp = nbt.getLong("lastContributionTimestamp");
        data.currentThreatLevel = nbt.getInt("currentThreatLevel");
        return data;
    }

    /**
     * Saves the current data to an NBT compound tag.
     * This method is called by the game when the data is marked as dirty and the world saves. [3]
     *
     * @param nbt The CompoundTag to write the data to.
     * @return The updated CompoundTag with this object's data.
     */
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("currentLevel", currentLevel);
        nbt.putLong("currentProgressPoints", currentProgressPoints);
        nbt.putLong("lastContributionTimestamp", lastContributionTimestamp);
        nbt.putInt("currentThreatLevel", currentThreatLevel);
        return nbt;
    }

    // --- GETTERS ---

    public int getCurrentLevel() {
        return currentLevel;
    }

    public long getCurrentProgressPoints() {
        return currentProgressPoints;
    }

    public long getLastContributionTimestamp() {
        return lastContributionTimestamp;
    }

    public int getCurrentThreatLevel() {
        return currentThreatLevel;
    }

    // --- SETTERS AND MODIFIERS ---
    // Each method that modifies data must call setDirty() to flag it for saving. [2, 3]

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
        setDirty();
    }

    public void setCurrentProgressPoints(long points) {
        this.currentProgressPoints = points;
        setDirty();
    }

    public void addProgressPoints(long pointsToAdd) {
        this.currentProgressPoints += pointsToAdd;
        setDirty();
    }

    public void setLastContributionTimestamp(long timestamp) {
        this.lastContributionTimestamp = timestamp;
        setDirty();
    }

    public void setCurrentThreatLevel(int threatLevel) {
        this.currentThreatLevel = threatLevel;
        setDirty();
    }
}