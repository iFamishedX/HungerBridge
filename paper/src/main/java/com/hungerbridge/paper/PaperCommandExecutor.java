package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command)
        );
    }

    @Override
    public List<String> executeWithOutput(String command) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            CapturingSender sender = new CapturingSender();
            plugin.getServer().dispatchCommand(sender, command);
            future.complete(sender.getLines());
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static class CapturingSender implements CommandSender {
        private final List<String> lines = new ArrayList<>();

        @Override
        public void sendMessage(String message) {
            lines.add(message);
        }

        public List<String> getLines() {
            return lines;
        }

        // All other methods: no-op or return defaults
        @Override public String getName() { return "HungerBridge"; }
        @Override public boolean isPermissionSet(String s) { return true; }
        @Override public boolean hasPermission(String s) { return true; }
        @Override public boolean isOp() { return true; }
        @Override public void setOp(boolean b) {}
        @Override public Server getServer() { return Bukkit.getServer(); }
        @Override public Spigot spigot() { return new Spigot(); }
    }
}
