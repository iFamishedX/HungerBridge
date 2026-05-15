package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class PaperCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin;

    public PaperCommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command
                )
        );
    }

    @Override
    public List<String> executeWithOutput(String command, boolean showConsole) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTask(plugin, () -> {

            List<String> lines = new ArrayList<>();

            // ROOT LOGGER — the only reliable logger for Purpur/Paper
            Logger root = (Logger) LogManager.getRootLogger();

            // Save all existing appenders (console, file, etc.)
            Map<String, Appender> originalAppenders = root.getAppenders();

            // Our capture appender
            Appender capture = new AbstractAppender(
                    "HungerBridgeCapture",
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

            if (!showConsole) {
                // Remove all appenders so NOTHING prints to console
                for (Appender app : originalAppenders.values()) {
                    root.removeAppender(app);
                }
            }

            root.addAppender(capture);

            try {
                // Execute the command
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command
                );
            } finally {
                // Remove capture appender
                root.removeAppender(capture);
                capture.stop();

                if (!showConsole) {
                    // Restore original appenders only if we removed them
                    for (Appender app : originalAppenders.values()) {
                        root.addAppender(app);
                    }
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
