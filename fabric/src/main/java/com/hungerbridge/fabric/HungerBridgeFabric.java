package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");

    @Override
    public void onInitializeServer() {
        LOGGER.info("HungerBridge Fabric mod initialized");

        // Register platform adapter; actual server startup can be wired later via mixin if needed
        Platform.setAdapter(new FabricPlatformAdapter());
    }
}
