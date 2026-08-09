package com.ender.qol.core;

import com.ender.qol.core.api.IQOLModule;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager responsible for registering and initializing active QOL modules.
 */
public class ModuleManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<IQOLModule> MODULES = new ArrayList<>();
    private static final List<IQOLModule> ACTIVE_MODULES = new ArrayList<>();

    public static void registerModule(IQOLModule module) {
        MODULES.add(module);
    }

    public static void initModules() {
        ACTIVE_MODULES.clear();
        for (IQOLModule module : MODULES) {
            if (module.isDependenciesPresent()) {
                ACTIVE_MODULES.add(module);
                module.registerEvents();
                LOGGER.info("[EnderQoL] Module '{}' ({}) loaded successfully.", module.getName(), module.getModuleId());
            } else {
                LOGGER.info("[EnderQoL] Module '{}' disabled: missing required dependencies {}.", 
                        module.getName(), String.join(", ", module.getRequiredModIds()));
            }
        }
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        for (IQOLModule module : ACTIVE_MODULES) {
            module.onCommonSetup(event);
        }
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        for (IQOLModule module : ACTIVE_MODULES) {
            module.onClientSetup(event);
        }
    }

    public static List<IQOLModule> getActiveModules() {
        return ACTIVE_MODULES;
    }
}
