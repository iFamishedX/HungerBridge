package com.hungerbridge.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

import java.nio.file.Path;

public final class AgentPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        try {
            // Locate this mod's JAR
            Path jarPath = FabricLoader.getInstance()
                    .getModContainer("hungerbridge")
                    .orElseThrow()
                    .getOrigin()
                    .getPaths()
                    .get(0);

            System.out.println("[HungerBridgeAgent] Attaching Java agent from " + jarPath);

            // Attach the embedded agent
            FabricLauncherBase.getLauncher().addJavaAgent(jarPath);
        } catch (Throwable t) {
            System.err.println("[HungerBridgeAgent] Failed to attach Java agent");
            t.printStackTrace();
        }
    }
}
