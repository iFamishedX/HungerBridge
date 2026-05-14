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
        return s.getServerDirectory().resolve("config/HungerBridge");
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd) -> {
            MinecraftServer s = (MinecraftServer) server;

            return s.submit(() -> {
                // NOTE: This executes the command but does NOT yet capture output text.
                // Proper output capture on Fabric 1.21.x realistically requires a mixin
                // into CommandSourceStack#sendSystemMessage. This is a clean hook for that.
                CommandSourceStack source = s.createCommandSourceStack();

                ParseResults<CommandSourceStack> parsed =
                        s.getCommands().getDispatcher().parse(cmd, source);

                s.getCommands().performCommand(parsed, cmd);

                // Placeholder: no textual output captured yet.
                return "";
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
