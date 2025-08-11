package com.dfrpg_gen.screen;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.networking.ModPackets;
import com.dfrpg_gen.networking.packet.ContributeItemPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(DfrpgGen.MOD_ID, "textures/gui/generator_gui.png");

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 1000; // Hides default label
        this.titleLabelY = 1000;   // Hides default label

        addRenderableWidget(Button.builder(Component.translatable("gui.dfrpg_gen.generator.contribute"), (button) -> {
            ItemStack stackInSlot = this.menu.getSlot(36).getItem();
            if(!stackInSlot.isEmpty()) {
                ModPackets.sendToServer(new ContributeItemPacket(stackInSlot.getItem(), stackInSlot.getCount()));
            }
        }).bounds(leftPos + 99, topPos + 57, 72, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x+1, y+2, 0, 0, imageWidth, imageHeight);

        renderProgressBars(guiGraphics, x, y);
    }

    private void renderProgressBars(GuiGraphics guiGraphics, int x, int y) {
        long globalProgress = this.menu.getCurrentProgress();
        long globalCost = this.menu.getLevelCost();
        if (globalCost > 0) {
            int progressWidth = (int) (162 * ((double)globalProgress / globalCost));
            guiGraphics.blit(TEXTURE, x + 7, y + 25, 0, 166, progressWidth, 12);
        }

        long playerProgress = this.menu.getPlayerContribution();
        // The player cap is 35% of the total cost
        long playerCap = (long)(globalCost * 0.35); // This should be read from config, but client doesn't know it. Approximating here.
        if (playerCap > 0) {
            int progressWidth = (int) (162 * ((double)playerProgress / playerCap));
            guiGraphics.blit(TEXTURE, x + 7, y + 42, 0, 178, progressWidth, 12);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Custom Labels
        guiGraphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 8, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.dfrpg_gen.generator.global_progress"), this.leftPos + 8, this.topPos + 15, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.dfrpg_gen.generator.personal_progress"), this.leftPos + 8, this.topPos + 32, 4210752, false);
    }
}