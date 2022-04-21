package ru.turikhay.tlauncher.bootstrap.bridge;

import java.util.UUID;

public final class BootEventDispatcher implements BootListener {
    private final BootBridge bridge;

    private boolean booting, working;
    private double percentage;

    private Throwable error;

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
        if(booting || working) {
            throw new IllegalStateException("booting: " + booting + "; working:" + working);
        }
        checkInterrupted();
        for (BootListener l : bridge.listenerList) {
            l.onBootStarted();
        }
        booting = true;
        log("Boot started");
    }

    @Override
    public void onBootStateChanged(String stepName, double percentage) throws InterruptedException {
        if(!booting || working) {
            throw new IllegalStateException("booting: " + booting + "; working:" + working);
        }
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
        if(!booting || working) {
            throw new IllegalStateException("booting: " + booting + "; working:" + working);
        }
        checkInterrupted();
        for (BootListener l : bridge.listenerList) {
            l.onBootSucceeded();
        }
        booting = false;
        working = true;
        log("Boot finished");
    }

    @Override
    public void onBootErrored(Throwable t) throws InterruptedException {
        for (BootListener l : bridge.listenerList) {
            l.onBootErrored(t);
        }

        booting = false;
        working = false;
        error = t;

        synchronized (this) {
            notifyAll();
        }

        log("Boot errored");
        t.printStackTrace();
    }

    void waitUntilClose() throws InterruptedException, BootException {
        checkInterrupted();

        while(booting || working) {
            synchronized (this) {
                wait();
            }
        }

        if(error != null) {
            throw new BootException(error);
        }
    }

    public void requestClose() {
        log("Close operation requested");

        working = false;
        synchronized(this) {
            notifyAll();
        }

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