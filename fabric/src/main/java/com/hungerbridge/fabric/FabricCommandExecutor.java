package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class FabricCommandExecutor implements CommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    private CommandSourceStack console() {
        // Default console source already has full permission
        return server.createCommandSourceStack();
    }

    @Override
    public void execute(String command) {
        server.execute(() -> {
            server.getCommands().performPrefixedCommand(console(), command);
        });
    }

    @Override
    public List<String> executeWithOutput(String command) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        server.execute(() -> {
            List<String> lines = new ArrayList<>();

            Logger root = (Logger) LogManager.getRootLogger();
            Map<String, Appender> original = root.getAppenders();

            for (Appender a : original.values()) {
                root.removeAppender(a);
            }

            Appender capture = new AbstractAppender(
                    "HungerBridgeFabricCapture",
                    null,
                    PatternLayout.newBuilder().withPattern("%msg").build(),
                    false,
                    null
            ) {
                @Override
                public void append(LogEvent event) {
                    if (event.getMessage() == null) return;
                    String msg = event.getMessage().getFormattedMessage();
                    if (msg == null) return;
                    String trimmed = msg.trim();
                    if (!trimmed.isEmpty()) {
                        lines.add(trimmed);
                    }
                }
            };

            capture.start();
            root.addAppender(capture);

            try {
                server.getCommands().performPrefixedCommand(console(), command);
            } finally {
                root.removeAppender(capture);
                capture.stop();
                for (Appender a : original.values()) {
                    root.addAppender(a);
                }
            }

            future.complete(List.copyOf(lines));
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
