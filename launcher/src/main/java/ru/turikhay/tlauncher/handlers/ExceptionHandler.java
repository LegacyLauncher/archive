package ru.turikhay.tlauncher.handlers;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.background.ImageBackground;
import ru.turikhay.util.U;

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
                    Sentry.capture(new EventBuilder()
                            .withLevel(Event.Level.ERROR)
                            .withMessage("uncaught exception: " + e)
                            .withSentryInterface(new ExceptionInterface(e))
                            .withExtra("thread", t.getName())
                    );
                    try {
                        Alert.showError("Exception in thread " + t.getName(), e);
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                } else {
                    LOGGER.debug("Hidden exception in thread {}", t.getName(), e);
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
