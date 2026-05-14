package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
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

            // Capture console output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;

            System.setOut(ps);
            System.setErr(ps);

            try {
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command
                );
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }

            String output = baos.toString();
            List<String> lines = Arrays.stream(output.split("\n")).toList();

            future.complete(lines);
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
