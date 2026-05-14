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

        // Result callback – in 1.21.x this is about success/result, not messages
        CommandResultCallback callback = new CommandResultCallback() {
            @Override
            public void onResult(boolean success, int result) {
                // no-op for now; we only care about messages
            }
        };

        CommandSourceStack source = server.createCommandSourceStack()
                .withCallback(callback);

        // NOTE: this does NOT capture messages yet; that would require
        // overriding sendSystemMessage on the source. For now, we just execute.
        server.getCommands().performPrefixedCommand(source, command);

        if (output.isEmpty()) {
            return "";
        }

        return String.join("\n", output);
    }
}
