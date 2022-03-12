package ru.turikhay.tlauncher.bootstrap.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task<T> implements Callable<T> {
    Task<?> bindingTask, boundTask;
    private double boundTaskProgressStart;
    private double boundTaskProgressDelta;

    private final AtomicBoolean executing = new AtomicBoolean();
    private volatile boolean interrupted;

    private List<TaskListener<? super T>> listeners = new ArrayList<>();

    private final String name;
    private final String logPrefix;

    public Task(String name) {
        this.name = name;
        logPrefix = "[" + getClass().getSimpleName() + ":" + name + ']';
    }

    public final String getName() {
        return name;
    }

    public final void addListener(TaskListener<? super T> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
    }

    public final Task<?> getBindingTask() {
        return bindingTask;
    }

    public final void interrupt() {
        log("interrupted");

        this.interrupted = true;
        interrupted();

        if (bindingTask != null) {
            bindingTask.interrupt();
        }
    }

    protected void interrupted() {
    }

    @Override
    public final T call() throws Exception {
        if (executing.getAndSet(true)) {
            throw new IllegalArgumentException("already executing");
        }

        listeners = Collections.unmodifiableList(listeners);

        for (TaskListener<? super T> listener : listeners) {
            listener.onTaskStarted(this);
        }

        Exception e = null;

        try {
            return execute();
        } catch (InterruptedException inEx) {
            interrupted = true;
            throw inEx;
        } catch (Exception ex) {
            throw e = ex;
        } finally {
            if (interrupted) {
                log("interruption confirmed");
                updateProgress(-1.);
                for (TaskListener<? super T> listener : listeners) {
                    listener.onTaskInterrupted(this);
                }
            } else {
                if (e == null) {
                    updateProgress(1.);
                    log("Done!");
                    for (TaskListener<? super T> listener : listeners) {
                        listener.onTaskSucceeded(this);
                    }
                } else {
                    log("Failed:", e);
                    for (TaskListener<? super T> listener : listeners) {
                        listener.onTaskErrored(this, e);
                    }
                }
            }
            listeners = new ArrayList<>();
            executing.set(false);

            unbind();
        }
    }

    private void unbind() throws Exception {
        bindTo(null, 0., 0.);
    }

    protected final <V> V bindTo(Task<V> task, double startProgress, double stopProgress) throws Exception {
        if (startProgress > stopProgress) {
            throw new IllegalArgumentException();
        }

        for (TaskListener<? super T> listener : listeners) {
            listener.onTaskBound(this, task);
        }

        if (task != null) {
            bindingTask = task;
            //log("binding to " + bindingTask);

            if (task.boundTask != null) {
                throw new IllegalStateException("task is already bound");
            }

            task.boundTask = this;
            task.boundTaskProgressStart = startProgress;
            task.boundTaskProgressDelta = stopProgress - startProgress;


            V result = task.call();

            updateProgress(stopProgress);
            unbind();
            checkInterrupted();

            return result;
        } else {
            //log("unbind from " + bindingTask);
            if (bindingTask != null) {
                if (bindingTask.boundTask == this) {
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
        if (percentage < 0. || percentage > 1.) {
            percentage = -1.;
        }

        progress = percentage;

        for (TaskListener<? super T> listener : listeners) {
            listener.onTaskUpdated(this, percentage);
        }

        if (boundTask != null) {
            boundTask.updateProgress(percentage == -1. ? boundTaskProgressStart : boundTaskProgressStart + percentage * boundTaskProgressDelta);
        }
    }

    public final boolean isExecuting() {
        return executing.get();
    }

    protected final void checkInterrupted() throws TaskInterruptedException {
        if (interrupted) {
            throw new TaskInterruptedException(this);
        }
    }

    protected final boolean isInterrupted() {
        return interrupted;
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name);
    }

    public final String toString() {
        return toStringBuilder().build();
    }

    protected abstract T execute() throws Exception;

    protected void log(Object... o) {
        U.log(logPrefix, o);
    }
}
