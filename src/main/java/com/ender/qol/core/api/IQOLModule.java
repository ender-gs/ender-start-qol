package com.ender.qol.core.api;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Common interface for all Ender QOL Modules.
 * Implement this interface to create modular feature sets that conditionally load
 * based on modpack dependencies.
 */
public interface IQOLModule {

    /**
     * Unique identifier for this module.
     */
    String getModuleId();

    /**
     * Display name for this module.
     */
    String getName();

    /**
     * List of required mod IDs for this module to function (e.g., "appeng", "buildinggadgets2").
     */
    String[] getRequiredModIds();

    /**
     * Checks whether all required mod dependencies are loaded in the environment.
     */
    default boolean isDependenciesPresent() {
        for (String modId : getRequiredModIds()) {
            if (!ModList.get().isLoaded(modId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called during FMLCommonSetupEvent if dependencies are met.
     */
    void onCommonSetup(FMLCommonSetupEvent event);

    /**
     * Called during FMLClientSetupEvent if dependencies are met.
     */
    void onClientSetup(FMLClientSetupEvent event);

    /**
     * Register Forge event bus listeners for this module.
     */
    void registerEvents();
}
