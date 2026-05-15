package com.hungerbridge.fabric;

import java.util.ArrayList;
import java.util.List;

public final class OutputCapture {

    private static final ThreadLocal<List<String>> BUFFER =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<Boolean> ACTIVE =
            ThreadLocal.withInitial(() -> false);

    public static void begin() {
        ACTIVE.set(true);
        BUFFER.get().clear();
    }

    public static void end() {
        ACTIVE.set(false);
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static void add(String line) {
        BUFFER.get().add(line);
    }

    public static List<String> get() {
        return new ArrayList<>(BUFFER.get());
    }
}
