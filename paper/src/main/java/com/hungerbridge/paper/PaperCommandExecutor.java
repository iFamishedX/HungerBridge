package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    // --- TPS / tick time ---

    @Override
    public double getTps() {
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length == 0) return -1.0;
        return tps[0];
    }

    @Override
    public double getTps1m() {
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length < 2) return -1.0;
        return tps[1];
    }

    @Override
    public double getTps5m() {
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length < 3) return -1.0;
        return tps[2];
    }

    @Override
    public double getTps15m() {
        // Paper exposes only 3 values; reuse 5m for 15m to keep schema stable.
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length < 3) return -1.0;
        return tps[2];
    }

    @Override
    public double getTickTimeMs() {
        long[] times = Bukkit.getServer().getTickTimes();
        if (times == null || times.length == 0) return -1.0;

        long avg = 0L;
        for (long t : times) avg += t;
        avg /= times.length;

        return avg / 1_000_000.0;
    }

    // --- Players ---

    @Override
    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return names;
    }
}
