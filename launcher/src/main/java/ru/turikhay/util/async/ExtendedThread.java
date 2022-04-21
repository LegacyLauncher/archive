package ru.turikhay.util.async;

import ru.turikhay.util.U;

import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedThread extends Thread {
    private static final AtomicInteger threadNum = new AtomicInteger();
    private volatile ExtendedThread.ExtendedThreadCaller caller;
    private String blockReason;
    private final Object monitor;

    public ExtendedThread(Runnable runnable, String name) {
        super(runnable, (name == null ? "ExtendedThread" : name) + "#" + threadNum.incrementAndGet());
        monitor = new Object();
        caller = new ExtendedThread.ExtendedThreadCaller();
    }

    public ExtendedThread(Runnable runnable) {
        this(runnable, null);
    }

    public ExtendedThread(String name) {
        this(null, name);
    }

    public ExtendedThread() {
        this(null, null);
    }

    public ExtendedThread.ExtendedThreadCaller getCaller() {
        return caller;
    }

    void setCaller(ExtendedThreadCaller caller) {
        this.caller = caller;
    }

    public void startAndWait() {
        super.start();

        while (!isThreadLocked()) {
            U.sleepFor(100L);
        }
    }

    protected void lockThread(String reason) {
        if (reason == null) {
            throw new NullPointerException();
        } else {
            checkCurrent();
            blockReason = reason;
            synchronized (monitor) {
                while (blockReason != null) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }
                }

            }
        }
    }

    public void unlockThread(String reason) {
        if (reason == null) {
            throw new NullPointerException();
        } else if (!reason.equals(blockReason)) {
            throw new IllegalStateException("Unlocking denied! Locked with: " + blockReason + ", tried to unlock with: " + reason);
        } else {
            blockReason = null;
            synchronized (monitor) {
                monitor.notify();
            }
        }
    }

    public void tryUnlock(String reason) {
        if (reason == null) {
            throw new NullPointerException();
        } else {
            if (reason.equals(blockReason)) {
                unlockThread(reason);
            }
        }
    }

    public boolean isThreadLocked() {
        return blockReason != null;
    }

    public boolean isCurrent() {
        return Thread.currentThread().equals(this);
    }

    protected void checkCurrent() {
        if (!isCurrent()) {
            throw new IllegalStateException("Illegal thread!");
        }
    }

    static class ExtendedThreadCaller extends RuntimeException {
        ExtendedThreadCaller() {
        }
    }
}
