package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HungerBridgeFabric implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");

    @Override
    public void onInitialize() {
        Platform.setAdapter(new FabricPlatformAdapter());

        LOGGER.info("HungerBridge Fabric mod initialized");
    }
}
