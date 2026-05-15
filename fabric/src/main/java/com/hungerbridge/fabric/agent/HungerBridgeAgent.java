package com.hungerbridge.fabric.agent;

import java.lang.instrument.Instrumentation;

public class HungerBridgeAgent {

    // Called when attached at JVM startup
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[HungerBridgeAgent] premain: installing transformer");
        inst.addTransformer(new Log4jTransformer(), true);
    }

    // Called when attached dynamically (not used, but safe to have)
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[HungerBridgeAgent] agentmain: installing transformer");
        inst.addTransformer(new Log4jTransformer(), true);
    }
}
