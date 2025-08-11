package com.dfrpg_gen.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides an instance of the IPlayerContribution capability.
 * This class is responsible for creating, storing, and exposing the capability
 * instance, as well as handling its serialization to and from NBT.
 */
public class PlayerContributionProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<IPlayerContribution> PLAYER_CONTRIBUTION =
            CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerContribution contribution = null;
    private final LazyOptional<IPlayerContribution> optional = LazyOptional.of(this::createPlayerContribution);

    private PlayerContribution createPlayerContribution() {
        if (this.contribution == null) {
            this.contribution = new PlayerContribution();
        }
        return this.contribution;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_CONTRIBUTION) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerContribution().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerContribution().loadNBTData(nbt);
    }
}