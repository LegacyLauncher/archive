package ru.turikhay.util.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.windows.wmi.WMI;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class WMIProvider {
    private static final Logger LOGGER = LogManager.getLogger(WMIProvider.class);

    private static final Future<List<String>> AV_LIST = AsyncThread.future(() -> {
        try {
            return WMI.getAVSoftwareList();
        } catch (NoClassDefFoundError noClassDefFoundError) {
            // ignore
            return Collections.emptyList();
        }
    });

    public static List<String> getAvSoftwareList() {
        try {
            return AV_LIST.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Could not fetch AV list", e);
            return Collections.emptyList();
        }
    }

    private WMIProvider() {
    }
}
