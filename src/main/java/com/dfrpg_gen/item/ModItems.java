package com.dfrpg_gen.item;

import com.dfrpg_gen.DfrpgGen;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Manages the registration of all mod items.
 * This class holds the DeferredRegister for items, which is used to queue items
 * for registration during the mod loading process. Other classes, like ModBlocks,
 * can use this register to add their corresponding items (e.g., BlockItems).
 */
public class ModItems {

    /**
     * The DeferredRegister for all items in this mod.
     * The actual registration objects (e.g., for BlockItems) are created in their
     * respective classes (like ModBlocks) but use this central register.
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DfrpgGen.MOD_ID);

    // --- Standalone Item Registration ---
    // This is where you would register custom items that are not blocks.
    // Example (currently commented out):
    // public static final RegistryObject<Item> SAPPHIRE = ITEMS.register("sapphire",
    //         () -> new Item(new Item.Properties()));


    /**
     * Registers the DeferredRegister for items with the mod's event bus.
     * This method must be called from the main mod constructor.
     * @param eventBus The mod's event bus, obtained from FMLJavaModLoadingContext.
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}