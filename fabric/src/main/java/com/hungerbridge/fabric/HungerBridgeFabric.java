package com.hungerbridge.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HungerBridgeFabric implements ModInitializer {

    public static final String MOD_ID = "hungerbridge";
    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");

    @Override
    public void onInitialize() {
        // Fabric Loader-only initialization (Mojang mappings mode)
        LOGGER.info("HungerBridge Fabric mod initialized");
    }
}
