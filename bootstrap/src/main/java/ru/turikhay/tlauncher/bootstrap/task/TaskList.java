package ru.turikhay.tlauncher.bootstrap.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public final class TaskList extends Task<Void> {
    private final ExecutorService service;
    private final boolean shutdownAfter;

    private final Map<Task<?>, Future<?>> taskMap = new HashMap<>();

    public TaskList(String name, ExecutorService service, boolean shutdownAfter) {
        super(name);
        this.service = Objects.requireNonNull(service, "service");
        this.shutdownAfter = shutdownAfter;
    }

    public TaskList(String name, int maxThreads) {
        this(name, Executors.newFixedThreadPool(maxThreads, getThreadFactory()), true);
    }

    public TaskList(String name) {
        this(name, Executors.newCachedThreadPool(getThreadFactory()), true);
    }

    public <T> Future<T> submit(Task<T> task) {
        if (this == task) {
            throw new IllegalArgumentException("cannot add task list to itself");
        }
        Future<T> future = service.submit(task);
        taskMap.put(task, future);
        return future;
    }

    public Set<Task<?>> getTaskSet() {
        return taskMap.keySet();
    }

    @Override
    protected Void execute() throws Exception {
        Exception error = null;

        if (!taskMap.isEmpty()) {
            boolean allDone;
            do {
                allDone = true;

                if (error != null || isInterrupted()) {
                    cancellAll();
                    break;
                }

                double progress = 0.0, currentProgress;
                for (Map.Entry<Task<?>, Future<?>> entry : taskMap.entrySet()) {
                    if (allDone &= entry.getValue().isDone()) {
                        try {
                            entry.getValue().get(); // check if computation did not threw an exception
                        } catch (CancellationException cancelled) {
                            // ignore
                        } catch (Exception e) {
                            error = e;
                        }
                    }
                    currentProgress = entry.getKey().getProgress();
                    if (currentProgress > 0.) {
                        progress += currentProgress;
                    }
                }
                updateProgress(progress / taskMap.size());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException interrupted) {
                    cancellAll();
                }
            } while (!allDone);
        }

        if (shutdownAfter) {
            service.shutdown();
        }

        if (error != null) {
            throw error;
        }

        checkInterrupted();

        return null;
    }

    private void cancellAll() {
        for (Task<?> task : taskMap.keySet()) {
            task.interrupt();
        }
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("map", taskMap);
    }

    private static ThreadFactory threadFactory;

    private static ThreadFactory getThreadFactory() {
        if (threadFactory == null) {
            threadFactory = r -> {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(ExceptionHandler.get());
                return t;
            };
        }
        return threadFactory;
    }
}