package com.hungerbridge.common;

import com.hungerbridge.common.util.Platform;

public class HungerBridgeCommon {

    private static Server httpServer;

    public static void onServerStart(Object platformServer) {
        try {
            Platform.ServerAdapter adapter = Platform.getServerAdapter();
            var mc = adapter.unwrap(platformServer);

            var cfgDir = adapter.getConfigDir(mc);
            var config = Config.load(cfgDir);

            Platform.init(
                    adapter.getCommandExecutor(mc),
                    Platform::log
            );

            httpServer = new Server(config);
            httpServer.start();

        } catch (Exception e) {
            Platform.log("error", "Failed to start HungerBridge: " + e.getMessage());
        }
    }

    public static void onServerStop() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
