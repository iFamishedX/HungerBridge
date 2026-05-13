package com.hungerbridge.fabric;

import com.hungerbridge.common.util.Platform;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class FabricPlatformAdapter implements Platform.ServerAdapter {

    @Override
    public Object unwrap(Object server) {
        return (MinecraftServer) server;
    }

    @Override
    public Path getConfigDir(Object server) {
        MinecraftServer mc = (MinecraftServer) server;
        return mc.getRunDirectory().toPath().resolve("config").resolve("HungerBridge");
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        MinecraftServer mc = (MinecraftServer) server;
        return (cmd, silent) -> mc.getCommands().performPrefixedCommand(
                mc.createCommandSourceStack().withSuppressedOutput(),
                cmd
        );
    }
}
