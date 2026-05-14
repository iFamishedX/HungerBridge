package com.hungerbridge.paper;

import com.hungerbridge.common.util.Platform;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                public String getName() {
                    return "HungerBridge";
                }

                @Override
                public Server getServer() {
                    return Bukkit.getServer();
                }

                @Override
                public boolean isOp() {
                    return true;
                }

                @Override
                public void setOp(boolean value) {
                    // ignore
                }

                @Override
                public boolean isPermissionSet(String name) {
                    return true;
                }

                @Override
                public boolean isPermissionSet(Permission perm) {
                    return true;
                }

                @Override
                public boolean hasPermission(String name) {
                    return true;
                }

                @Override
                public boolean hasPermission(Permission perm) {
                    return true;
                }

                @Override
                public PermissionAttachment addAttachment(Plugin plugin) {
                    return null;
                }

                @Override
                public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
                    return null;
                }

                @Override
                public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
                    return null;
                }

                @Override
                public void removeAttachment(PermissionAttachment attachment) {
                }

                @Override
                public void recalculatePermissions() {
                }

                @Override
                public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                    return Set.of();
                }
            };

            try {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    Bukkit.dispatchCommand(capturingSender, cmd);
                    return null;
                }).get();
            } catch (Exception e) {
                Bukkit.getLogger().severe("HungerBridge command execution failed: " + e.getMessage());
                return "";
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
