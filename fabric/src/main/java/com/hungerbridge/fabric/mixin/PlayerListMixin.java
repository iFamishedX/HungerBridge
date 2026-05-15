package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    static {
        System.out.println("[HungerBridge/DIAG] PlayerListMixin LOADED (class loaded)");
    }

    @Inject(
            method = "broadcastSystemMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hungerbridge_captureBroadcast(Component message, boolean overlay, CallbackInfo ci) {
        System.out.println("[HungerBridge/DIAG] broadcastSystemMessage FIRED: " + message.getString());

        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel(); // stop it from hitting console
        }
    }
}
