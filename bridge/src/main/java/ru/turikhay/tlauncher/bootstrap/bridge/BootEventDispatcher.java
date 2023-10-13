package ru.turikhay.tlauncher.bootstrap.bridge;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public final class BootEventDispatcher implements BootListener {
    private final BootBridge bridge;

    private double percentage;

    private Throwable error;

    private final CountDownLatch closeLatch = new CountDownLatch(1);

    BootEventDispatcher(BootBridge bridge) {
        if(bridge == null) {
            throw new NullPointerException("bridge");
        }
        this.bridge = bridge;
    }

    public void passClient(UUID client) {
        bridge.setClient(client);
    }

    public Throwable getError() {
        return error;
    }

    public BootMessage getBootMessage(String locale) {
        return bridge.getMessage(locale);
    }

    @Override
    public void onBootStarted() throws InterruptedException {
        checkInterrupted();
        for (BootListener l : bridge.listenerList) {
            l.onBootStarted();
        }
        log("Boot started");
    }

    @Override
    public void onBootStateChanged(String stepName, double percentage) throws InterruptedException {
        if(percentage < this.percentage) {
            throw new IllegalArgumentException("percentage is lower than prevoius value: " + percentage + " (expecting bigger than " + this.percentage + ")");
        }
        if(percentage > 1.0) {
            throw new IllegalArgumentException("percentage is above 1.0");
        }
        checkInterrupted();
        for (BootListener l : bridge.listenerList) {
            l.onBootStateChanged(stepName, percentage);
        }
        this.percentage = percentage;
        log("Boot state changed: \"" + stepName + "\", " + percentage);
    }

    @Override
    public void onBootSucceeded() throws InterruptedException {
        checkInterrupted();
        for (BootListener l : bridge.listenerList) {
            l.onBootSucceeded();
        }
        log("Boot finished");
    }

    @Override
    public void onBootErrored(Throwable t) throws InterruptedException {
        for (BootListener l : bridge.listenerList) {
            l.onBootErrored(t);
        }

        error = t;

        closeLatch.countDown();

        log("Boot errored");
        t.printStackTrace();
    }

    void waitUntilClose() throws InterruptedException, BootException {
        checkInterrupted();

        closeLatch.await();

        if(error != null) {
            throw new BootException(error);
        }
    }

    public void requestClose() {
        log("Close operation requested");

        closeLatch.countDown();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
            log("Closing forcefully!");
            System.exit(-1);
        }).start();
    }

    private void checkInterrupted() throws InterruptedException {
        if(bridge.interrupted) {
            throw new InterruptedException("external interrupt");
        }
    }

    private void log(String s) {
        System.out.println("[BootEventDisp] " + s);
    }
}