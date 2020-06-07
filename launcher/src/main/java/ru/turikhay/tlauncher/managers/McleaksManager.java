package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.util.U;

public final class McleaksManager {
    private static final boolean SUPPORTED;
    private static final McleaksStatus STATUS;
    private static final McleaksConnector CONNECTOR;

    static {
        boolean supported = true;
        try {
            ru.turikhay.app.nstweaker.NSTweaker.checkSupported();
        } catch (Throwable t) {
            U.log("MCLeaks tweaks are not supported", t);
            supported = false;
            Sentry.sendError(McleaksManager.class, "not supported", t, null);
        }
        SUPPORTED = supported;
        STATUS = supported? new McleaksStatus() : null;
        CONNECTOR = supported? new McleaksConnector() : null;
    }

    public static void checkSupported() {
        if(!SUPPORTED) {
            throw new IllegalStateException("not supported");
        }
    }

    public static boolean isUnsupported() {
        return !SUPPORTED;
    }

    public static McleaksStatus getStatus() {
        checkSupported();
        return STATUS;
    }

    public static McleaksConnector getConnector() {
        checkSupported();
        return CONNECTOR;
    }

    public static void triggerConnection() {
        if(SUPPORTED) {
            getStatus().triggerFetch();
        }
    }
}
