package com.hungerbridge.fabric.mixin;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import com.hungerbridge.fabric.FabricCommandExecutor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");
    private Server hungerBridgeServer;

    @Inject(method = "setupServer", at = @At("RETURN"))
    private void hungerbridge$onServerStart(CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = (MinecraftServer)(Object)this;

        try {
            Path cfgDir = server.getRunDirectory().toPath().resolve("config").resolve("HungerBridge");
            Config config = Config.load(cfgDir);

            FabricCommandExecutor exec = new FabricCommandExecutor(server);

            Platform.init(
                    (cmd, silent) -> exec.run(cmd, silent),
                    (level, msg) -> switch (level.toLowerCase()) {
                        case "error" -> LOGGER.error(msg);
                        case "warn", "warning" -> LOGGER.warn(msg);
                        case "debug" -> LOGGER.debug(msg);
                        case "trace" -> LOGGER.trace(msg);
                        default -> LOGGER.info(msg);
                    }
            );

            hungerBridgeServer = new Server(config);
            hungerBridgeServer.start();
            LOGGER.info("HungerBridge HTTP server started on port {}", config.port);

        } catch (Exception e) {
            LOGGER.error("Failed to start HungerBridge", e);
        }
    }
}
