package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
            CapturingSender sender = new CapturingSender();
            plugin.getServer().dispatchCommand(sender, command);
            future.complete(sender.lines);
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static final class CapturingSender implements CommandSender {

        private final List<String> lines = new ArrayList<>();

        // ----------- MESSAGE CAPTURE -----------
        @Override
        public void sendMessage(String message) {
            lines.add(message);
        }

        @Override
        public void sendMessage(Component component) {
            lines.add(component.toString());
        }

        // ----------- REQUIRED BY PAPER 1.21.x -----------
        @Override
        public String getName() {
            return "HungerBridge";
        }

        @Override
        public Component name() {
            return Component.text("HungerBridge");
        }

        @Override
        public Spigot spigot() {
            return new Spigot();
        }

        // ----------- MINIMAL REQUIRED IMPLEMENTATIONS -----------
        @Override public boolean isPermissionSet(String s) { return true; }
        @Override public boolean hasPermission(String s) { return true; }
        @Override public boolean isOp() { return true; }
        @Override public void setOp(boolean b) {}
        @Override public org.bukkit.Server getServer() { return Bukkit.getServer(); }
    }
}
