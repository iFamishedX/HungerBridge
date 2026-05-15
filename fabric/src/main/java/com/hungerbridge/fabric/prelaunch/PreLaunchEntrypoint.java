package com.hungerbridge.fabric.prelaunch;

public class PreLaunchEntrypoint implements Runnable {
    @Override
    public void run() {
        System.out.println("[HB/PreLaunch] PreLaunch mixins active");
    }
}
