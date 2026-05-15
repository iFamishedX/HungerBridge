package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public final class FabricCommandExecutor implements CommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void execute(String command) {
        server.execute(() ->
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(), command
                )
        );
    }

    @Override
    public List<String> executeWithOutput(String command) {
        List<String> lines = new ArrayList<>();

        server.execute(() -> {
            CommandSourceStack source =
                    server.createCommandSourceStack().withCallback(new CommandSource() {
                        @Override
                        public void sendSystemMessage(Component message) {
                            lines.add(message.getString());
                        }

                        @Override
                        public boolean acceptsSuccess() { return true; }

                        @Override
                        public boolean acceptsFailure() { return true; }

                        @Override
                        public boolean shouldInformAdmins() { return false; }
                    });

            server.getCommands().performPrefixedCommand(source, command);
        });

        return lines;
    }
}
