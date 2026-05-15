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
        method = "method_43496",   // obfuscated name
        at = @At("HEAD"),
        cancellable = true,
        remap = false              // DO NOT remap this name
    )
    private void hungerbridge_captureCommandOutput(Component message, CallbackInfo ci) {
        OutputCapture.add(message.getString());
        ci.cancel(); // suppress console output
    }
}
