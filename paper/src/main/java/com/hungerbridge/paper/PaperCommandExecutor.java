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
    public List<String> executeWithOutput(String command) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTask(plugin, () -> {

            List<String> lines = new ArrayList<>();

            Logger root = (Logger) LogManager.getRootLogger();

            Appender appender = new AbstractAppender(
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

                    // Remove HungerBridge prefix ONLY if present
                    if (msg.startsWith("[HungerBridge] ")) {
                        msg = msg.substring("[HungerBridge] ".length());
                    }

                    // Hide HungerBridge internal logs from console
                    if (msg.startsWith("Executing command via /v1/run:")) {
                        return; // do not print to console
                    }

                    String trimmed = msg.trim();
                    if (!trimmed.isEmpty()) {
                        lines.add(trimmed);
                    }
                }
            };

            appender.start();
            root.addAppender(appender);

            try {
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command
                );
            } finally {
                root.removeAppender(appender);
                appender.stop();
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
