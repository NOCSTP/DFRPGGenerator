package com.dfrpg_gen.block;

import com.dfrpg_gen.DfrpgGen;
import com.dfrpg_gen.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Manages the registration of all mod blocks.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DfrpgGen.MOD_ID);

    public static final RegistryObject<Block> GENERATOR_BLOCK = registerBlock("generator_block",
            () -> new GeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)));


    /**
     * Helper method to register a block and its corresponding BlockItem.
     * @param name The registry name for the block.
     * @param block A supplier for the block instance.
     * @return A RegistryObject for the registered block.
     * @param <T> The type of the block being registered.
     */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Helper method to register a BlockItem for a given block.
     * @param name The registry name.
     * @param block The RegistryObject of the block.
     * @return A RegistryObject for the registered item.
     * @param <T> The type of the block.
     */
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /**
     * Registers the DeferredRegister for blocks with the mod event bus.
     * @param eventBus The mod's event bus.
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}