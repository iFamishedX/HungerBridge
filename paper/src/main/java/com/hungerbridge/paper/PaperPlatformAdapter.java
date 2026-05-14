package com.hungerbridge.paper;

import com.hungerbridge.common.util.Platform;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PaperPlatformAdapter implements Platform.ServerAdapter {

    private final Plugin plugin;

    public PaperPlatformAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Path getConfigDir(Object server) {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd) -> {
            final List<String> output = new ArrayList<>();

            // Custom sender that captures output
            CommandSender capturingSender = new CommandSender() {

                @Override
                public void sendMessage(String message) {
                    output.add(message);
                }

                @Override
                public void sendMessage(String[] messages) {
                    for (String m : messages) output.add(m);
                }

                @Override
                public void sendMessage(Component message) {
                    output.add(message.toString());
                }

                @Override
                public void sendMessage(Component... messages) {
                    for (Component m : messages) output.add(m.toString());
                }

                // Required boilerplate
                @Override public String getName() { return "HungerBridge"; }
                @Override public boolean isPermissionSet(String s) { return true; }
                @Override public boolean isPermissionSet(org.bukkit.permissions.Permission p) { return true; }
                @Override public boolean hasPermission(String s) { return true; }
                @Override public boolean hasPermission(org.bukkit.permissions.Permission p) { return true; }
                @Override public org.bukkit.Server getServer() { return Bukkit.getServer(); }
                @Override public org.bukkit.command.Spigot spigot() { return Bukkit.getConsoleSender().spigot(); }
                @Override public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin) { return null; }
                @Override public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin, int ticks) { return null; }
                @Override public org.bukkit.permissions.PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) { return null; }
                @Override public void removeAttachment(org.bukkit.permissions.PermissionAttachment attachment) {}
                @Override public void recalculatePermissions() {}
                @Override public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { return java.util.Collections.emptySet(); }
                @Override public boolean isOp() { return true; }
                @Override public void setOp(boolean value) {}
            };

            try {
                // Ensure sync execution
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    Bukkit.dispatchCommand(capturingSender, cmd);
                    return null;
                }).get();
            } catch (Exception e) {
                Bukkit.getLogger().severe("HungerBridge command execution failed: " + e.getMessage());
            }

            if (output.isEmpty()) {
                return "";
            }

            return String.join("\n", output);
        };
    }

    @Override
    public Platform.Logger getLogger() {
        return (level, msg) -> {
            switch (level.toLowerCase()) {
                case "error" -> Bukkit.getLogger().severe(msg);
                case "warn", "warning" -> Bukkit.getLogger().warning(msg);
                case "debug" -> Bukkit.getLogger().info("[DEBUG] " + msg);
                case "trace" -> Bukkit.getLogger().info("[TRACE] " + msg);
                default -> Bukkit.getLogger().info(msg);
            }
        };
    }
}
