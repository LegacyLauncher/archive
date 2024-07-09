package net.legacylauncher.bootstrap.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final ExceptionHandler instance = new ExceptionHandler();

    public static ExceptionHandler get() {
        return instance;
    }

    private ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Error at {}", t.getName(), e);
    }
}
