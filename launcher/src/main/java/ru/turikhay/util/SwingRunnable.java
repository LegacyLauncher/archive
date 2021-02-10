package ru.turikhay.util;

public interface SwingRunnable extends Runnable {
    void doRun() throws Exception;

    @Override
    default void run() throws SuppressedSwingException {
        try {
            doRun();
        } catch (Exception e) {
            throw new SuppressedSwingException(e);
        }
    }
}
