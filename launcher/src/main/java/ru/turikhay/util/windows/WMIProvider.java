package ru.turikhay.util.windows;

import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.windows.wmi.WMI;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class WMIProvider {
    private static final Future<List<String>> AV_LIST = AsyncThread.future(new Callable<List<String>>() {
        public List<String> call() throws Exception {
            try {
                return WMI.getAVSoftwareList();
            } catch(NoClassDefFoundError noClassDefFoundError) {
                // ignore
                return Collections.emptyList();
            }
        }
    });

    public static List<String> getAvSoftwareList() {
        try {
            return AV_LIST.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            U.log(e);
            return Collections.EMPTY_LIST;
        }
    }

    private WMIProvider() {
    }
}
