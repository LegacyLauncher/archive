package net.legacylauncher.handlers;

import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.background.ImageBackground;
import net.legacylauncher.util.U;
import org.apache.logging.log4j.LogManager;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    private static ExceptionHandler instance;
    private static long gcLastCall;

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }

        return instance;
    }

    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof OutOfMemoryError)) return;
        try {
            OutOfMemoryError asOOM = (OutOfMemoryError) e;
            if (!reduceMemory(asOOM)) {
                if (scanTrace(e) && toShowError(e)) {
                    try {
                        Alert.showError("Exception in thread " + t.getName(), e);
                    } catch (Exception e1) {
                        LOGGER.warn("Cannot show alert window", e1);
                    }
                } else {
                    LOGGER.debug("Hidden exception in thread {}", t.getName(), e);
                }
            }
        } catch (Throwable e1) {
            LOGGER.error("Something gone totally wrong, I'll die soon ;(", e1);
        }
    }

    public static boolean reduceMemory(OutOfMemoryError e) {
        if (e == null) {
            return false;
        }

        LOGGER.fatal("OutOfMemory error has occurred");

        if (ImageBackground.getLastInstance() != null) {
            ImageBackground.getLastInstance().wipe();
        }

        long currentTime = System.currentTimeMillis();
        long diff = Math.abs(currentTime - gcLastCall);

        if (diff > 5000L) {
            gcLastCall = currentTime;
            LOGGER.info("Starting garbage collector: " + U.memoryStatus());
            System.gc();
            LOGGER.info("Garbage collector completed: " + U.memoryStatus());
            return true;
        } else {
            LOGGER.fatal("GC is unable to reduce memory usage");
            return false;
        }
    }

    private static boolean scanTrace(Throwable e) {
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().startsWith("ru.turikhay")) {
                return true;
            }
        }

        return false;
    }

    private static final String[] lastMessages = new String[10];
    private static int lastWrittenMessage = -1;

    private synchronized static boolean toShowError(Throwable t) {
        if (t == null) {
            return false;
        }

        String message = t.toString();

        for (String lastMessage : lastMessages) {
            if (message.equals(lastMessage)) {
                return false;
            }
        }

        lastWrittenMessage = (lastWrittenMessage + 1) % lastMessages.length;
        lastMessages[lastWrittenMessage] = message;

        return true;
    }
}
