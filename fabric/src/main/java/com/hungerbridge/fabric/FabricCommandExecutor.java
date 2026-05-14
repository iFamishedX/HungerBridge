package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public final class FabricCommandExecutor implements CommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void execute(String command) {
        server.execute(() ->
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(), command
                )
        );
    }

    @Override
    public List<String> executeWithOutput(String command) {
        execute(command);
        return null; // Fabric cannot capture output
    }
}
