package ru.turikhay.util.async;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class AsyncThread {
    private static final Logger LOGGER = LogManager.getLogger(AsyncThread.class);

    public static final ExecutorService SHARED_SERVICE = Executors.newCachedThreadPool(
            RunnableThread::new
    );

    public static void execute(Runnable r) {
        SHARED_SERVICE.execute(wrap(r));
    }

    public static void execute(Callable<Void> c) {
        SHARED_SERVICE.submit(c);
    }

    public static Future future(Runnable r) {
        return SHARED_SERVICE.submit(wrap(r));
    }

    public static <V> Future<V> future(Callable<V> c) {
        return SHARED_SERVICE.submit(wrap(c));
    }

    public static <V> Future<V> timeout(long timeout, TimeUnit timeUnit, Callable<V> c) {
        return future(() -> {
            Future<V> f = AsyncThread.future(c);
            try {
                return f.get(timeout, timeUnit);
            } catch(InterruptedException | TimeoutException e) {
                f.cancel(true);
                throw e;
            }
        });
    }

    public static <V> Future<V> timeoutSeconds(long timeout, Callable<V> c) {
        return timeout(timeout, TimeUnit.SECONDS, c);
    }

    public static <V> Future<V> after(long timeout, TimeUnit timeUnit, Callable<V> c) {
        return future(() -> {
            Thread.sleep(timeUnit.toMillis(timeout));
            return c.call();
        });
    }

    public static <V> Future<V> afterSeconds(long timeout, Callable<V> c) {
        return after(timeout, TimeUnit.SECONDS, c);
    }

    public static Future<?> after(long timeout, TimeUnit timeUnit, Runnable r) {
        return future(() -> {
            Thread.sleep(timeUnit.toMillis(timeout));
            r.run();
            return null; // hint to compiler to use Callable<>
        });
    }

    public static Future<?> afterSeconds(long timeout, Runnable r) {
        return after(timeout, TimeUnit.SECONDS, r);
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
