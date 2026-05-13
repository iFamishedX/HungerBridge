package com.hungerbridge.paper;

import com.hungerbridge.common.util.Platform;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.nio.file.Path;

public class PaperPlatformAdapter implements Platform.ServerAdapter {

    @Override
    public Object unwrap(Object server) {
        return (Server) server;
    }

    @Override
    public Path getConfigDir(Object server) {
        Server bukkit = (Server) server;
        return bukkit.getWorldContainer().toPath().resolve("plugins").resolve("HungerBridge");
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd, silent) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
