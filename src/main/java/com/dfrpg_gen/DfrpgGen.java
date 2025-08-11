package com.dfrpg_gen;

import com.dfrpg_gen.block.ModBlocks;
import com.dfrpg_gen.block.entity.ModBlockEntities;
import com.dfrpg_gen.config.ConfigManager;
import com.dfrpg_gen.config.ModConfigs;
import com.dfrpg_gen.event.RewardListener;
import com.dfrpg_gen.item.ModItems;
import com.dfrpg_gen.networking.ModPackets;
import com.dfrpg_gen.screen.GeneratorScreen;
import com.dfrpg_gen.screen.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DfrpgGen.MOD_ID)
public class DfrpgGen {
    public static final String MOD_ID = "dfrpg_gen";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DfrpgGen() {
        // ... (rest of the constructor is unchanged)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Deferred Registers for items, blocks, etc.
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Register setup methods for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);

        // Register the master configuration file
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC, "dfrpg_gen-common.toml");

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        // Register the custom Reward Listener
        MinecraftForge.EVENT_BUS.register(new RewardListener());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPackets.register();
        });
        LOGGER.info("DFRPG-GEN: Common setup initiated.");
    }

    public void onConfigLoad(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ModConfigs.SPEC) {
            ConfigManager.bake();
        }
    }

    // You can use a separate class for client-only events
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        }
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}