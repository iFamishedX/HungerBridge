package com.hungerbridge.fabric.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.Arrays;

@Mixin(DedicatedServer.class)
public class DedicatedServerMethodDumpMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dumpMethods(CallbackInfo ci) {
        System.out.println("[HB/DIAG] Dumping methods for DedicatedServer:");
        for (Method m : DedicatedServer.class.getDeclaredMethods()) {
            System.out.println("[HB/DIAG] method: " + m.getName()
                    + " params=" + Arrays.toString(m.getParameterTypes()));
        }
    }
}
