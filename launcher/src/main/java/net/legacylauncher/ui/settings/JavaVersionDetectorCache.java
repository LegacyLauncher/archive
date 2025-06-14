package net.legacylauncher.ui.settings;

import net.legacylauncher.util.JavaVersionDetector;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.shared.JavaVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class JavaVersionDetectorCache {
    private final Map<String, Future<JavaVersion>> taskCache = new HashMap<>();
    private final Runnable callback;

    public JavaVersionDetectorCache(Runnable callback) {
        this.callback = callback;
    }

    public synchronized Future<JavaVersion> get(String javaExec) {
        return taskCache.computeIfAbsent(javaExec, this::createTask);
    }

    private Future<JavaVersion> createTask(String javaExec) {
        return AsyncThread.future(new Task(callback, javaExec));
    }

    private static class Task implements Callable<JavaVersion> {
        private final Runnable callback;
        private final String javaExec;

        private Task(Runnable callback, String javaExec) {
            this.callback = callback;
            this.javaExec = javaExec;
        }

        @Override
        public JavaVersion call() throws Exception {
            try {
                JavaVersionDetector detector = new JavaVersionDetector(javaExec);
                return detector.detect();
            } finally {
                SwingUtil.later(callback);
            }
        }
    }
}
