package com.dfrpg_gen.screen;

import com.dfrpg_gen.block.ModBlocks;
import com.dfrpg_gen.block.entity.GeneratorBlockEntity;
import com.dfrpg_gen.capability.PlayerContributionProvider;
import com.dfrpg_gen.config.ConfigManager;
import com.dfrpg_gen.data.saved_data.HopeWorldData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class GeneratorMenu extends AbstractContainerMenu {
    public final GeneratorBlockEntity blockEntity;
    private final Level level;

    // Data slots for syncing data to the client
    private final DataSlot currentProgressLow;
    private final DataSlot currentProgressHigh;
    private final DataSlot levelCostLow;
    private final DataSlot levelCostHigh;
    private final DataSlot playerContributionLow;
    private final DataSlot playerContributionHigh;

    public GeneratorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public GeneratorMenu(int id, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.GENERATOR_MENU.get(), id);
        checkContainerSize(inv, 1);
        this.blockEntity = (GeneratorBlockEntity) entity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 80, 58));
        });

        // Initialize and add data slots
        this.currentProgressLow = DataSlot.standalone();
        this.currentProgressHigh = DataSlot.standalone();
        this.levelCostLow = DataSlot.standalone();
        this.levelCostHigh = DataSlot.standalone();
        this.playerContributionLow = DataSlot.standalone();
        this.playerContributionHigh = DataSlot.standalone();

        this.addDataSlot(this.currentProgressLow);
        this.addDataSlot(this.currentProgressHigh);
        this.addDataSlot(this.levelCostLow);
        this.addDataSlot(this.levelCostHigh);
        this.addDataSlot(this.playerContributionLow);
        this.addDataSlot(this.playerContributionHigh);

        updateData(inv.player);
    }

    // Helper to combine two ints from DataSlots back into a long
    private long getLongFromDataSlots(DataSlot low, DataSlot high) {
        return ((long)high.get() << 32) | (low.get() & 0xFFFFFFFFL);
    }

    public long getCurrentProgress() {
        return getLongFromDataSlots(currentProgressLow, currentProgressHigh);
    }

    public long getLevelCost() {
        return getLongFromDataSlots(levelCostLow, levelCostHigh);
    }

    public long getPlayerContribution() {
        return getLongFromDataSlots(playerContributionLow, playerContributionHigh);
    }

    /**
     * Updates the data slots with the latest server data.
     * Must be called on the server.
     */
    public void updateData(Player player) {
        if(player instanceof ServerPlayer serverPlayer) {
            HopeWorldData worldData = HopeWorldData.get(serverPlayer.getServer());
            long currentProgress = worldData.getCurrentProgressPoints();
            long cost = ConfigManager.getLevelCost(worldData.getCurrentLevel());

            this.currentProgressLow.set((int) (currentProgress & 0xFFFFFFFFL));
            this.currentProgressHigh.set((int) (currentProgress >> 32));
            this.levelCostLow.set((int) (cost & 0xFFFFFFFFL));
            this.levelCostHigh.set((int) (cost >> 32));

            player.getCapability(PlayerContributionProvider.PLAYER_CONTRIBUTION).ifPresent(cap -> {
                long pCont = cap.getTotalContributionToCurrentLevel();
                this.playerContributionLow.set((int) (pCont & 0xFFFFFFFFL));
                this.playerContributionHigh.set((int) (pCont >> 32));
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // ... (standard quickMoveStack implementation)
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.GENERATOR_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}