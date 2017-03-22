package ru.turikhay.tlauncher.bootstrap.bridge;

public interface BootListener {
    void onBootStarted() throws InterruptedException;
    void onBootStateChanged(String stepName, double percentage) throws InterruptedException;
    void onBootSucceeded() throws InterruptedException;
    void onBootErrored(Throwable t) throws InterruptedException;
}
