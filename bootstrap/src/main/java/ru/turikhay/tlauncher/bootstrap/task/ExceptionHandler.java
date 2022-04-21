package ru.turikhay.tlauncher.bootstrap.task;

import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.util.U;

public final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final ExceptionHandler instance = new ExceptionHandler();

    public static ExceptionHandler get() {
        return instance;
    }

    private ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Bootstrap.SENTRY.sendEvent(new EventBuilder()
                .withLevel(Event.Level.ERROR)
                .withMessage("uncaught exception: " + e.toString())
                .withSentryInterface(new ExceptionInterface(e))
                .withExtra("thread", t.getName())
        );
        U.log("[ExceptionHandler]", "Error at " + t.getName());
        e.printStackTrace();
    }
}
