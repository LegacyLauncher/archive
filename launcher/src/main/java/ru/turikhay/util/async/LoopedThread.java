package ru.turikhay.util.async;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LoopedThread extends ExtendedThread {
    protected static final String LOOPED_BLOCK = "iteration";

    private final AtomicBoolean working = new AtomicBoolean(true);

    public LoopedThread(String name) {
        super(name);
    }

    public LoopedThread() {
        this("LoopedThread");
    }

    protected final void lockThread(String reason) {
        if (reason == null) {
            throw new NullPointerException();
        } else if (!reason.equals("iteration")) {
            throw new IllegalArgumentException("Illegal block reason. Expected: iteration, got: " + reason);
        } else {
            super.lockThread(reason);
        }
    }

    public final boolean isIterating() {
        return !isThreadLocked();
    }

    public void iterate() {
        if (!isIterating()) {
            unlockThread("iteration");
        }
    }

    public final void run() {
        while (working.get()) {
            lockThread("iteration");
            iterateOnce();
        }
    }

    public final void dispose() {
        working.set(false);
        unlockThread("iteration");
    }

    protected abstract void iterateOnce();
}
