package com.hungerbridge.fabric;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class FabricCommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    public String run(String command) {
        List<String> output = new ArrayList<>();

        CommandResultCallback callback = new CommandResultCallback() {
            @Override
            public void sendMessage(CommandSourceStack source, Component message) {
                output.add(message.getString());
            }
        };

        CommandSourceStack source = server.createCommandSourceStack()
                .withCallback(callback);

        server.getCommands().performPrefixedCommand(source, command);

        if (output.isEmpty()) {
            return "";
        }

        return String.join("\n", output);
    }
}
