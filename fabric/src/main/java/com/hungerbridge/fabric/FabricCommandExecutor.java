package com.hungerbridge.fabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FabricCommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    public String run(String command, boolean silent) {
        if (silent) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);
            return "";
        }

        CapturingSource source = new CapturingSource(server.getCommandSource());
        server.getCommandManager().execute(source, command);
        return String.join("\n", source.getLines());
    }

    private static class CapturingSource extends ServerCommandSource {
        private final List<String> lines = new ArrayList<>();

        public CapturingSource(ServerCommandSource parent) {
            super(parent.getExecutor(), parent.getPosition(), parent.getRotation(),
                    parent.getWorld(), parent.getLevel(), parent.getName().getString(),
                    parent.getDisplayName(), parent.getServer(), parent.getEntity());
        }

        @Override
        public void sendMessage(Text message) {
            lines.add(message.getString());
        }

        public List<String> getLines() {
            return lines;
        }
    }
}
