package ru.turikhay.util.async;

import java.util.concurrent.*;

public class AsyncThread {
    public static final ExecutorService SHARED_SERVICE = Executors.newCachedThreadPool(
            ExtendedThread::new
    );
    public static final ScheduledExecutorService DELAYER = Executors.newSingleThreadScheduledExecutor();

    public static void execute(Runnable r) {
        SHARED_SERVICE.execute(wrap(r));
    }

    public static void execute(Callable<Void> c) {
        SHARED_SERVICE.submit(c);
    }

    public static Future<?> future(Runnable r) {
        return SHARED_SERVICE.submit(wrap(r));
    }

    public static <V> Future<V> future(Callable<V> c) {
        return SHARED_SERVICE.submit(wrap(c));
    }

    public static <V> CompletableFuture<V> completableFuture(Callable<V> c) {
        CompletableFuture<V> f = new CompletableFuture<>();
        AsyncThread.execute(() -> {
            V v;
            try {
                v = c.call();
            } catch (Throwable t) {
                f.completeExceptionally(t);
                return;
            }
            f.complete(v);
        });
        return f;
    }

    public static Future<?> after(long timeout, TimeUnit timeUnit, Runnable r) {
        return DELAYER.schedule(r, timeout, timeUnit);
    }

    public static Future<?> afterSeconds(long timeout, Runnable r) {
        return after(timeout, TimeUnit.SECONDS, r);
    }

    private static Runnable wrap(final Runnable r) {
        final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();
        return () -> {
            setupCaller(caller);
            r.run();
        };
    }

    private static <V> Callable<V> wrap(final Callable<V> c) {
        final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();
        return () -> {
            setupCaller(caller);
            return c.call();
        };
    }

    private static void setupCaller(ExtendedThread.ExtendedThreadCaller caller) {
        if (Thread.currentThread() instanceof ExtendedThread) {
            ((ExtendedThread) Thread.currentThread()).setCaller(caller);
        }
    }
}
