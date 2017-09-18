package ru.turikhay.util.async;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import java.util.concurrent.*;

public class AsyncThread {
    private static ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new RunnableThread(r);
        }
    });

    public static void execute(Runnable r) {
        service.execute(r);
    }

    public static Future future(Runnable r) {
        return service.submit(r);
    }

    public static <V> Future<V> future(Callable<V> c) {
        return service.submit(c);
    }

    private static Runnable wrap(final Runnable r) {
        final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();
        return new Runnable() {
            @Override
            public void run() {
                if(Thread.currentThread() instanceof ExtendedThread) {
                    ((ExtendedThread) Thread.currentThread()).setCaller(caller);
                }
                r.run();
            }
        };
    }
}
