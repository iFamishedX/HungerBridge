package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    @Inject(
            method = "sendSystemMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hungerbridge_captureSystemMessage(Component message, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel();
        }
    }
}
