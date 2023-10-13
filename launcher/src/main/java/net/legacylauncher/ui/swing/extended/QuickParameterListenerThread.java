package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.swing.util.IntegerArrayGetter;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.LoopedThread;

public class QuickParameterListenerThread extends LoopedThread {
    public static final int DEFAULT_TICK = 500;
    private final IntegerArrayGetter paramGetter;
    private final Runnable runnable;
    private final int tick;

    QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run, int tick) {
        super("QuickParameterListenerThread");
        if (getter == null) {
            throw new NullPointerException("Getter is NULL!");
        } else if (run == null) {
            throw new NullPointerException("Runnable is NULL!");
        } else if (tick < 0) {
            throw new IllegalArgumentException("Tick must be positive!");
        } else {
            paramGetter = getter;
            runnable = run;
            this.tick = tick;
            setPriority(1);
            startAndWait();
        }
    }

    QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run) {
        this(getter, run, 500);
    }

    void startListening() {
        iterate();
    }

    protected void iterateOnce() {
        int[] initial = SwingUtil.waitAndReturn(paramGetter::getIntegerArray);
        boolean i = false;

        boolean equal;
        do {
            sleep();
            int[] newvalue = SwingUtil.waitAndReturn(paramGetter::getIntegerArray);
            equal = true;

            for (int var5 = 0; var5 < initial.length; ++var5) {
                if (initial[var5] != newvalue[var5]) {
                    equal = false;
                    break;
                }
            }

            initial = newvalue;
        } while (!equal);

        SwingUtil.wait(runnable::run);
    }

    private void sleep() {
        U.sleepFor(tick);
    }
}
