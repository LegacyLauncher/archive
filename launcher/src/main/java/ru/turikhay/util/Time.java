package ru.turikhay.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import static ru.turikhay.util.Time.NotBoolean.*;

public final class Time {
    private static final ArrayList<TimeHolder> holders = new ArrayList<>();

    public static <T> T start(T object) {
        synchronized (holders) {
            Iterator<TimeHolder> i = holders.iterator();
            TimeHolder holder;

            while (i.hasNext()) {
                holder = i.next();
                switch (holder.isHolding(object)) {
                    case TRUE:
                        throw new IllegalStateException("object is already being held");
                    case UNDEFINED:
                        i.remove();
                }
            }
            holders.add(new TimeHolder(object));
        }
        return object;
    }

    private static long stop(Object object, long currentTime) {
        synchronized (holders) {
            Iterator<TimeHolder> i = holders.iterator();
            TimeHolder holder;

            while (i.hasNext()) {
                holder = i.next();
                if (holder.isHolding(object) == TRUE) {
                    long delta = currentTime - holder.timestamp;
                    i.remove();

                    return delta;
                }
            }
        }
        return Long.MIN_VALUE;
    }

    public static long stop(Object object) {
        return stop(object, System.currentTimeMillis());
    }

    public static long[] stop(Object... object) {
        long currentTime = System.currentTimeMillis();
        long[] deltas = new long[object.length];

        synchronized (holders) {
            for (int i = 0; i < object.length; i++) {
                deltas[i] = stop(object[i], currentTime);
            }
        }

        return deltas;
    }

    public static void start() {
        start(Thread.currentThread());
    }

    public static long stop() {
        return stop(Thread.currentThread());
    }

    private static class TimeHolder {
        private final WeakReference<Object> ref;
        private final long timestamp;

        TimeHolder(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }

            ref = new WeakReference<>(o);
            timestamp = System.currentTimeMillis();
        }

        NotBoolean isHolding(Object object) {
            final Object o = ref.get();
            if (o == null) {
                return UNDEFINED;
            }
            return o == object ? TRUE : FALSE;
        }
    }

    enum NotBoolean {
        FALSE, TRUE, UNDEFINED
    }

    private Time() {
    }
}
