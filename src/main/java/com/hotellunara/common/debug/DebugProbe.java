package com.hotellunara.common.debug;

import java.util.Map;

public final class DebugProbe {

    private DebugProbe() {
    }

    public static void log(String channel, String hint, String source, String message, Map<String, ?> details) {
        // No-op placeholder for optional debug instrumentation.
    }
}
