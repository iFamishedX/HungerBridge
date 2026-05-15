package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

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
            CommandSourceStack source = new CommandSourceStack(
                    new CommandSource() {
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
                    },
                    Vec3.ZERO,
                    Vec2.ZERO,
                    server.overworld(),
                    4,
                    "HungerBridge",
                    Component.literal("HungerBridge"),
                    server,
                    null
            );

            server.getCommands().performPrefixedCommand(source, command);
        });

        return lines;
    }
}
