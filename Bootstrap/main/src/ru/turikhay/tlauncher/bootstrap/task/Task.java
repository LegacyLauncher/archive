package ru.turikhay.tlauncher.bootstrap.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task<T> implements Callable<T> {
    private Task bindingTask, boundTask;
    private double boundTaskProgressStart;
    private double boundTaskProgressPrice;

    private final AtomicBoolean executing = new AtomicBoolean();

    private List<TaskListener> listenerList = new ArrayList<TaskListener>();
    private TaskListener[] listenersArray;

    private final String name;
    private final String logPrefix;

    public Task(String name) {
        this.name = name;
        logPrefix = "[Task:" + name + ']';
    }

    public final void addListener(TaskListener listener) {
        U.requireNotNull(listener, "listener");
        if (listenersArray != null) {
            throw new IllegalStateException();
        }
        listenerList.add(listener);
    }

    @Override
    public final T call() throws Exception {
        if (executing.getAndSet(true)) {
            throw new IllegalArgumentException("already executing");
        }

        listenersArray = new TaskListener[listenerList.size()];
        listenerList.toArray(listenersArray);

        if (listenersArray != null) {
            for (TaskListener listener : listenersArray) {
                listener.onTaskStarted(this);
            }
        }

        Exception e = null;

        try {
            log("Starting to execute: " + this);
            return execute();
        } catch (Exception ex) {
            throw (e = ex);
        } finally {
            if (e == null) {
                log("Done: " + this);
                for (TaskListener listener : listenersArray) {
                    listener.onTaskSucceeded(this);
                }
            } else {
                log("Errored: " + this);
                for (TaskListener listener : listenersArray) {
                    listener.onTaskErrored(this, e);
                }
            }

            listenerList.clear();
            listenersArray = null;
            bindTo(null, 0., 0.);

            executing.set(false);
        }
    }

    protected final <V> V bindTo(Task<V> task, double startProgress, double stopProgress) throws Exception {
        if (listenersArray != null) {
            for (TaskListener listener : listenersArray) {
                listener.onTaskBound(this, task);
            }
        }

        if (task != null) {
            bindingTask = task;
            log("binding to " + bindingTask);

            if (task.boundTask != null) {
                throw new IllegalStateException("task is already bound");
            }

            task.boundTask = this;
            task.boundTaskProgressStart = startProgress;
            task.boundTaskProgressPrice = 1. - (stopProgress - startProgress);
            return task.call();
        } else {
            log("unbind from " + bindingTask);
            if(bindingTask != null) {
                if(bindingTask.boundTask == this) {
                    bindingTask.boundTask = null;
                }
                bindingTask = null;
            }
        }

        return null;
    }

    private double progress;

    public final double getProgress() {
        return progress;
    }

    protected final void updateProgress(double percentage) {
        log("progress updated:", percentage);
        progress = percentage;

        if (listenersArray != null) {
            for (TaskListener listener : listenersArray) {
                listener.onTaskUpdated(this, percentage);
            }
        }

        if (boundTask != null) {
            boundTask.updateProgress(percentage == -1. ? boundTaskProgressStart : boundTaskProgressStart + percentage * boundTaskProgressPrice);
        }
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("executing", executing)
                .append("bindingTo", bindingTask)
                .append("boundTask", boundTask);
    }

    public final String toString() {
        return toStringBuilder().build();
    }

    protected abstract T execute() throws Exception;

    protected void log(Object... o) {
        U.log(logPrefix, o);
    }
}
