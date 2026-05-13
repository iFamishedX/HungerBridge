package com.hungerbridge.fabric;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HungerBridgeFabric implements ModInitializer {

    public static final String MOD_ID = "hungerbridge";
    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");

    private Server server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            try {
                Path cfgDir = minecraftServer.getRunDirectory().toPath().resolve("config").resolve("HungerBridge");
                Config config = Config.load(cfgDir);

                FabricCommandExecutor exec = new FabricCommandExecutor(minecraftServer);

                Platform.init(
                        (cmd, silent) -> exec.run(cmd, silent),
                        (level, msg) -> {
                            switch (level.toLowerCase()) {
                                case "error" -> LOGGER.error(msg);
                                case "warn", "warning" -> LOGGER.warn(msg);
                                case "debug" -> LOGGER.debug(msg);
                                case "trace" -> LOGGER.trace(msg);
                                default -> LOGGER.info(msg);
                            }
                        }
                );

                server = new Server(config);
                server.start();
                LOGGER.info("HungerBridge HTTP server started on port {}", config.port);
            } catch (Exception e) {
                LOGGER.error("Failed to start HungerBridge", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            if (server != null) {
                server.stop();
            }
        });
    }
}
