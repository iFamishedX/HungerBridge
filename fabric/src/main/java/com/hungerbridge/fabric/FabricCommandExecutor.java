package com.hungerbridge.fabric;

import net.minecraft.server.MinecraftServer;

/**
 * Optional helper if you want a direct executor separate from the adapter.
 * Not strictly required by the common core, but kept for symmetry.
 */
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
        // Same limitation as adapter: no textual output captured yet.
        return ok ? "" : "";
    }
}
