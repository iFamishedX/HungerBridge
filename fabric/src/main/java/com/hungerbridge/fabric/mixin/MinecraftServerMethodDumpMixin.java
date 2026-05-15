package com.hungerbridge.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.Arrays;

@Mixin(MinecraftServer.class)
public class MinecraftServerMethodDumpMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dumpMethods(CallbackInfo ci) {
        System.out.println("[HB/DIAG] Dumping methods for MinecraftServer:");
        for (Method m : MinecraftServer.class.getDeclaredMethods()) {
            System.out.println("[HB/DIAG] method: " + m.getName()
                    + " params=" + Arrays.toString(m.getParameterTypes()));
        }
    }
}
