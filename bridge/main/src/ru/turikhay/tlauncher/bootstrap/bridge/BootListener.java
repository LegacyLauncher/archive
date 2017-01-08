package ru.turikhay.tlauncher.bootstrap.bridge;

public interface BootListener {
    void onBootStarted();
    void onBootStateChanged(String stepName, double percentage);
    void onBootSucceeded();
    void onBootErrored(Throwable t);
}
