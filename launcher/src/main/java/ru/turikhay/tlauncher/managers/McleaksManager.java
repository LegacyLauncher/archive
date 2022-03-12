package ru.turikhay.tlauncher.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class McleaksManager {
    private static final Logger LOGGER = LogManager.getLogger(McleaksManager.class);

    private static final boolean SUPPORTED;
    private static final McleaksStatus STATUS;
    private static final McleaksConnector CONNECTOR;

    static {
        boolean supported = true;
        try {
            ru.turikhay.app.nstweaker.NSTweaker.checkSupported();
        } catch (Throwable t) {
            LOGGER.info("MCLeaks will not be available", t);
            supported = false;
        }
        SUPPORTED = supported;
        STATUS = supported ? new McleaksStatus() : null;
        CONNECTOR = supported ? new McleaksConnector() : null;
    }

    public static void checkSupported() {
        if (!SUPPORTED) {
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
        if (SUPPORTED) {
            getStatus().triggerFetch();
        }
    }
}
