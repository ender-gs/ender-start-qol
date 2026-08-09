package com.ender.qol.bg2pattern;

import com.ender.qol.core.api.IQOLModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AE2BG2Module implements IQOLModule {

    private final SchematicToPatternConverter schematicToPatternConverter = new SchematicToPatternConverter();

    @Override
    public String getModuleId() {
        return "ae2-bg2";
    }

    @Override
    public String getName() {
        return "AE2 & Building Gadgets 2 Integration Module";
    }

    @Override
    public String[] getRequiredModIds() {
        return new String[]{"appeng", "buildinggadgets2", "ae2wtlib"};
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        // Common setup logic for AE2-BG2 integration
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        // Client setup logic for AE2-BG2 integration
    }

    @Override
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(schematicToPatternConverter);
    }
}
