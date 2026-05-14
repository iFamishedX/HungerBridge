package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class FabricPlatformAdapter implements Platform.ServerAdapter {

    @Override
    public Path getConfigDir(Object server) {
        MinecraftServer s = (MinecraftServer) server;
        return s.getServerDirectory().toPath().resolve("config/HungerBridge");
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd, silent) -> {
            MinecraftServer s = (MinecraftServer) server;

            return s.submit(() -> {
                CommandSourceStack source = s.createCommandSourceStack();

                // Brigadier parse step
                ParseResults<CommandSourceStack> parsed =
                        s.getCommands().getDispatcher().parse(cmd, source);

                // Execute parsed command
                int result = s.getCommands().performCommand(parsed, cmd);

                return result == 1 ? "1" : "0";
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
