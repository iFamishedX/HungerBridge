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
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd) -> {
            boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return ok ? "1" : "0";
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
