package ru.turikhay.util.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AsyncThread {
    private static ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new RunnableThread(r);
        }
    });

    public static void execute(Runnable r) {
        service.execute(r);
    }
}
