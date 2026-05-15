package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
    private void hungerbridge$interceptConsoleOutput(Component message, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel(); // suppress printing to console
        }
    }
}
