package com.hungerbridge.fabric;

import net.minecraft.server.MinecraftServer;

public class FabricCommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    public String run(String command) {
        boolean ok = server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(),
                command
        );

        return ok ? "1" : "0";
    }
}
