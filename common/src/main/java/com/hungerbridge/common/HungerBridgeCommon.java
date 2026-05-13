package com.hungerbridge.common;

import net.minecraft.server.MinecraftServer;

public class HungerBridgeCommon {

    private static Server httpServer;

    public static void onServerStart(MinecraftServer mcServer) {
        try {
            var cfgDir = mcServer.getRunDirectory().toPath().resolve("config").resolve("HungerBridge");
            var config = Config.load(cfgDir);

            var exec = new Platform.CommandExecutor() {
                @Override
                public void run(String cmd, boolean silent) {
                    mcServer.getCommands().performPrefixedCommand(
                            mcServer.createCommandSourceStack().withSuppressedOutput(),
                            cmd
                    );
                }
            };

            Platform.init(
                    exec,
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
