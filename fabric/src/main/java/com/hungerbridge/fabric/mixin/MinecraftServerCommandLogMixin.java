package com.hungerbridge.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerCommandLogMixin {

    @Inject(
        method = "logMessage",   // THIS is method_43496 in Yarn
        at = @At("HEAD"),
        cancellable = true
    )
    private void hungerbridge_captureCommandOutput(Text message, CallbackInfo ci) {
        com.hungerbridge.fabric.HungerBridgeFabric.onCommandLog(message.getString());
        ci.cancel(); // suppress console output
    }
}
