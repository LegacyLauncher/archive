package ru.turikhay.util.async;

import java.util.concurrent.*;

public class AsyncThread {
    private static ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new RunnableThread(r);
        }
    });

    public static void execute(Runnable r) {
        service.execute(wrap(r));
    }

    public static Future future(Runnable r) {
        return service.submit(wrap(r));
    }

    public static <V> Future<V> future(Callable<V> c) {
        return service.submit(wrap(c));
    }

    private static Runnable wrap(final Runnable r) {
        final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();
        return new Runnable() {
            @Override
            public void run() {
                setupCaller(caller);
                r.run();
            }
        };
    }

    private static <V> Callable<V> wrap(final Callable<V> c) {
        final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();
        return new Callable<V>() {
            @Override
            public V call() throws Exception {
                setupCaller(caller);
                return c.call();
            }
        };
    }

    private static void setupCaller(ExtendedThread.ExtendedThreadCaller caller) {
        if(Thread.currentThread() instanceof ExtendedThread) {
            ((ExtendedThread) Thread.currentThread()).setCaller(caller);
        }
    }
}
