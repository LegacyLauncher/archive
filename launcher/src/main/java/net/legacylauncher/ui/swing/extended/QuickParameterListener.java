package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.swing.util.IntegerArrayGetter;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QuickParameterListener {
    private final IntegerArrayGetter paramGetter;
    private final Runnable callback;
    private int[] lastValue;
    private ScheduledFuture<?> future;

    QuickParameterListener(IntegerArrayGetter getter, Runnable callback) {
        if (getter == null) {
            throw new NullPointerException("Getter is NULL!");
        } else if (callback == null) {
            throw new NullPointerException("Runnable is NULL!");
        }
        this.paramGetter = getter;
        this.callback = callback;
    }

    void startListening() {
        if (future != null) {
            return;
        }
        lastValue = queryNow();
        future = AsyncThread.DELAYER.scheduleWithFixedDelay(this::schedulePool, 1, 1, TimeUnit.SECONDS);
    }

    void dispose() {
        future.cancel(true);
        future = null;
    }

    private void schedulePool() {
        AsyncThread.execute(this::pool);
    }

    private void pool() {
        int[] value = queryNow();
        if (Arrays.equals(lastValue, value)) {
            return;
        }
        lastValue = value;
        SwingUtil.later(callback);
    }

    private int[] queryNow() {
        return SwingUtil.waitAndReturn(paramGetter::getIntegerArray);
    }
}
