package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        OutputCapture.begin();

        // Use a future so we know exactly when the command finishes
        CompletableFuture<Void> future = new CompletableFuture<>();

        server.execute(() -> {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(), command
            );
            future.complete(null);
        });

        // Wait for the command to finish
        try {
            future.get(); // blocks until the server thread completes the command
        } catch (Exception ignored) {}

        OutputCapture.end();
        return OutputCapture.drain();
    }
}
