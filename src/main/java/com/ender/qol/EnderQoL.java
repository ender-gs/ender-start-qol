package com.ender.qol;

import com.ender.qol.bg2pattern.AE2BG2Module;
import com.ender.qol.core.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnderQoL.MOD_ID)
public class EnderQoL {
    public static final String MOD_ID = "enderqol";
    public static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("removal")
    public EnderQoL() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register available modules
        ModuleManager.registerModule(new AE2BG2Module());

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Ender QOL Mod initialized.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModuleManager.initModules();
        ModuleManager.onCommonSetup(event);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModuleManager.onClientSetup(event);
    }
}
