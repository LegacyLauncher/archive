package net.legacylauncher.util;

@FunctionalInterface
public interface SwingRunnable extends Runnable {
    void doRun() throws Exception;

    @Override
    default void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new SwingRunnableException(e);
        }
    }
}
