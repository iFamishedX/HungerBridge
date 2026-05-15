package com.hungerbridge.fabric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OutputCapture {

    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<List<String>> BUFFER = ThreadLocal.withInitial(ArrayList::new);

    private OutputCapture() {}

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
        if (ACTIVE.get()) {
            BUFFER.get().add(line);
        }
    }

    public static List<String> drain() {
        List<String> out = new ArrayList<>(BUFFER.get());
        BUFFER.get().clear();
        return Collections.unmodifiableList(out);
    }
}
