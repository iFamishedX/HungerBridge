package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class FabricPlatformAdapter implements Platform.ServerAdapter {

    @Override
    public Path getConfigDir(Object server) {
        return ((MinecraftServer) server).getRunDirectory().toPath().resolve("config/HungerBridge");
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd, silent) -> {
            MinecraftServer s = (MinecraftServer) server;

            // Run command on main thread
            return s.submit(() -> {
                boolean ok = s.getCommandManager().executeWithPrefix(
                        s.getCommandSource(),
                        cmd
                );
                return ok ? "1" : "0";
            }).join();
        };
    }

    @Override
    public Platform.Logger getLogger() {
        return (level, msg) -> {
            switch (level.toLowerCase()) {
                case "error" -> System.err.println("[HungerBridge] " + msg);
                case "warn", "warning" -> System.out.println("[HungerBridge WARN] " + msg);
                case "debug" -> System.out.println("[HungerBridge DEBUG] " + msg);
                case "trace" -> System.out.println("[HungerBridge TRACE] " + msg);
                default -> System.out.println("[HungerBridge] " + msg);
            }
        };
    }
}
