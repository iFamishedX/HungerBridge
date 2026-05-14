package com.hungerbridge.common;

import com.hungerbridge.common.util.Platform;

import java.nio.file.Path;

public class HungerBridgeCommon {

    private Server httpServer;

    public void start(Object server) {
        try {
            // Adapter must already be set by Paper/Fabric
            var adapter = Platform.adapter();

            Path cfgDir = adapter.getConfigDir(server);
            Config config = Config.load(cfgDir);

            // Initialize executor + logger from adapter
            Platform.init(
                    adapter.getCommandExecutor(server),
                    adapter.getLogger()
            );

            httpServer = new Server(config);
            httpServer.start();

            Platform.logger().log("info",
                    "HungerBridge HTTP server started on port " + config.port);

        } catch (Exception e) {
            Platform.logger().log("error",
                    "Failed to start HungerBridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
