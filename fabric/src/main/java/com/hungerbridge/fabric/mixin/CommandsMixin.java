package com.hungerbridge.fabric.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandBuildContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void hungerbridge$registerCommands(Commands.CommandSelection selection,
                                               CommandBuildContext context,
                                               CallbackInfo ci) {
        // If you want to register commands in Mojang mappings, do it here.
        // Example:
        // HungerBridgeCommon.registerCommands(context, selection);
    }
}
