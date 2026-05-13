package com.hungerbridge.fabric;

import net.minecraft.server.MinecraftServer;

public class FabricCommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    public void run(String command, boolean silent) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(),
                command
        );
    }
}
