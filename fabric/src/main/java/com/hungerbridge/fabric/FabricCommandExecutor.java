package com.hungerbridge.fabric;

import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.fabric.mixin.MinecraftServerMixin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class FabricCommandExecutor implements CommandExecutor {

    private final MinecraftServer server;

    public FabricCommandExecutor(MinecraftServer server) {
        this.server = server;
    }

    private CommandSourceStack console() {
        return server.createCommandSourceStack();
    }

    @Override
    public void execute(String command) {
        server.execute(() ->
                server.getCommands().performPrefixedCommand(console(), command)
        );
    }

    @Override
    public List<String> executeWithOutput(String command, boolean showConsole) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        server.execute(() -> {
            List<String> lines = new ArrayList<>();

            Logger root = (Logger) LogManager.getRootLogger();
            Map<String, Appender> original = root.getAppenders();

            Appender capture = new AbstractAppender(
                    "HungerBridgeFabricCapture",
                    null,
                    PatternLayout.newBuilder().withPattern("%msg").build(),
                    false,
                    null
            ) {
                @Override
                public void append(LogEvent event) {
                    if (event.getMessage() == null) return;
                    String msg = event.getMessage().getFormattedMessage();
                    if (msg == null) return;
                    String trimmed = msg.trim();
                    if (!trimmed.isEmpty()) {
                        lines.add(trimmed);
                    }
                }
            };

            capture.start();

            if (!showConsole) {
                for (Appender a : original.values()) {
                    root.removeAppender(a);
                }
            }

            root.addAppender(capture);

            try {
                server.getCommands().performPrefixedCommand(console(), command);
            } finally {
                root.removeAppender(capture);
                capture.stop();

                if (!showConsole) {
                    for (Appender a : original.values()) {
                        root.addAppender(a);
                    }
                }
            }

            future.complete(List.copyOf(lines));
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }


    private long[] getTickTimes() {
        return ((MinecraftServerMixin)(Object)server).hungerbridge$getTickTimes();
    }

    @Override
    public double getTps() {
        long[] nanos = getTickTimes();
        if (nanos == null || nanos.length == 0) return -1.0;

        long avg = 0L;
        for (long t : nanos) avg += t;
        avg /= nanos.length;

        double ms = avg / 1_000_000.0;
        if (ms <= 0.0) return -1.0;

        double tps = 1000.0 / ms;
        return Math.min(20.0, tps);
    }

    @Override
    public double getTps1m() {
        return getTps();
    }

    @Override
    public double getTps5m() {
        return getTps();
    }

    @Override
    public double getTps15m() {
        return getTps();
    }

    @Override
    public double getTickTimeMs() {
        long[] nanos = getTickTimes();
        if (nanos == null || nanos.length == 0) return -1.0;

        long avg = 0L;
        for (long t : nanos) avg += t;
        avg /= nanos.length;

        return avg / 1_000_000.0;
    }


    @Override
    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Component name = player.getName();
            names.add(name.getString());
        }
        return names;
    }
}
