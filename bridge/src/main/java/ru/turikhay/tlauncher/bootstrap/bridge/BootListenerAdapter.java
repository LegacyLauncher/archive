package ru.turikhay.tlauncher.bootstrap.bridge;

public class BootListenerAdapter implements BootListener {
    @Override
    public void onBootStarted() {
    }

    @Override
    public void onBootStateChanged(String stepName, double percentage) {
    }

    @Override
    public void onBootSucceeded() {
    }

    @Override
    public void onBootErrored(Throwable t) {
    }
}
