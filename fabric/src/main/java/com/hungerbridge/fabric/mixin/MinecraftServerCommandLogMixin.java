// fabric/src/main/java/com/hungerbridge/fabric/mixin/MinecraftServerCommandLogMixin.java
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
        method = "sendSystemMessage", // maps to method_43496
        at = @At("HEAD")
    )
    private void hungerbridge_captureCommandOutput(Text message, CallbackInfo ci) {
        // mirror whatever Paper side does with the command log
        com.hungerbridge.fabric.HungerBridgeFabric.onCommandLog(message.getString());
    }
}
