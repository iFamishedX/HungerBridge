package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerCommandLogMixin {

    @Inject(
        method = "sendSystemMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hungerbridge$interceptSendSystemMessage(Component message, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel();
        }
    }
}
