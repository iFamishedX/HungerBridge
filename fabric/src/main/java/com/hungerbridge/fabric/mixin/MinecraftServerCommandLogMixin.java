package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerCommandLogMixin {

    static {
        System.out.println("[HB/DIAG] MinecraftServerCommandLogMixin LOADED");
    }

    @Inject(
            method = "method_43496",   // raw obfuscated name
            at = @At("HEAD"),
            cancellable = true,
            remap = false              // DO NOT map the name
    )
    private void hungerbridge_captureCommandOutput(Component message, CallbackInfo ci) {
        System.out.println("[HB/DIAG] method_43496 FIRED: " + message.getString());

        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel();
        }
    }
}
