package ru.turikhay.tlauncher.bootstrap.task;

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
        U.log("[ExceptionHandler]", "Error at " + t.getName());
        e.printStackTrace();
    }
}
