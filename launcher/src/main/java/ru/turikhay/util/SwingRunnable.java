package ru.turikhay.util;

public interface SwingRunnable extends Runnable {
    void doRun() throws Exception;

    @Override
    default void run() throws SwingRunnableException {
        try {
            doRun();
        } catch (Exception e) {
            throw new SwingRunnableException(e);
        }
    }
}
