package com.hungerbridge.paper;

import com.hungerbridge.common.util.Platform;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

public class PaperPlatformAdapter implements Platform.ServerAdapter {

    private final Plugin plugin;

    public PaperPlatformAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Path getConfigDir(Object server) {
        // Use the plugin's data folder as the config root
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd, silent) -> {
            final String[] result = { "0" };

            try {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    boolean ok = Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            cmd
                    );
                    result[0] = ok ? "1" : "0";
                    return null;
                }).get();
            } catch (Exception e) {
                Bukkit.getLogger().severe("HungerBridge command execution failed: " + e.getMessage());
                result[0] = "0";
            }

            return result[0];
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
