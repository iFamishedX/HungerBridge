package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    static {
        System.out.println("[HungerBridge/DIAG] DedicatedServerMixin LOADED (class loaded)");
    }

    @Inject(
            method = "logCommandMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hungerbridge_captureLog(Component message, CallbackInfo ci) {
        System.out.println("[HungerBridge/DIAG] logCommandMessage FIRED: " + message.getString());

        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel(); // stop console spam
        }
    }
}
