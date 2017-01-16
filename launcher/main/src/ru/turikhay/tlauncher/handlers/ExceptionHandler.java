package ru.turikhay.tlauncher.handlers;

import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.logger.Logger;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private static ExceptionHandler instance;
    private static long gcLastCall;

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }

        return instance;
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            OutOfMemoryError asOOM = Reflect.cast(e, OutOfMemoryError.class);
            if (asOOM == null || !reduceMemory(asOOM)) {
                if (scanTrace(e) && toShowError(e)) {
                    try {
                        Sentry.sendError(ExceptionHandler.class, "uncaughtException", e, null);
                        Alert.showError("Exception in thread " + t.getName(), e);
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                } else {
                    U.log("Hidden exception in thread " + t.getName(), e);
                }
            }
        } catch (Throwable t0) {
            t0.printStackTrace();
        }
    }

    public static boolean reduceMemory(OutOfMemoryError e) {
        if (e == null) {
            return false;
        }

        U.log("OutOfMemory error has occurred, solving...");

        Logger.wipeAll();

        long currentTime = System.currentTimeMillis();
        long diff = Math.abs(currentTime - gcLastCall);

        if (diff > 5000L) {
            gcLastCall = currentTime;
            U.gc();
            return true;
        } else {
            U.log("GC is unable to reduce memory usage");
            return false;
        }
    }

    private static boolean scanTrace(Throwable e) {
        StackTraceElement[] elements = e.getStackTrace();
        StackTraceElement[] var5 = elements;
        int var4 = elements.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            StackTraceElement element = var5[var3];
            if (element.getClassName().startsWith("ru.turikhay")) {
                return true;
            }
        }

        return false;
    }

    private static String[] lastMessages = new String[10];
    private static int lastWrittenMessage = -1;

    private synchronized static boolean toShowError(Throwable t) {
        if (t == null) {
            return false;
        }

        String message = t.toString();

        for (int i = 0; i < lastMessages.length; i++) {
            if (message.equals(lastMessages[i])) {
                return false;
            }
        }

        lastWrittenMessage = (lastWrittenMessage == lastMessages.length - 1) ? 0 : lastWrittenMessage + 1;
        lastMessages[lastWrittenMessage] = message;

        return true;
    }
}
