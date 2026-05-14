package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
                List<String> output = new ArrayList<>();

                // Capture output from the command source
                CommandResultCallback callback = new CommandResultCallback() {
                    @Override
                    public void sendMessage(CommandSourceStack source, Component message) {
                        output.add(message.getString()); // EXACT output, no prefix
                    }
                };

                CommandSourceStack source = s.createCommandSourceStack()
                        .withCallback(callback);

                ParseResults<CommandSourceStack> parsed =
                        s.getCommands().getDispatcher().parse(cmd, source);

                s.getCommands().performCommand(parsed, cmd);

                if (output.isEmpty()) {
                    return ""; // No prefix, no filler
                }

                return String.join("\n", output);
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
