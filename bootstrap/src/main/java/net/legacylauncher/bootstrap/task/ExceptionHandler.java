package net.legacylauncher.bootstrap.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final ExceptionHandler instance = new ExceptionHandler();

    public static ExceptionHandler get() {
        return instance;
    }

    private ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Error at {}", t.getName(), e);
    }
}
