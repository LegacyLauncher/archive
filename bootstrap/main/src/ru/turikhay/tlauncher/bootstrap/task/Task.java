package ru.turikhay.tlauncher.bootstrap.task;

import shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import shaded.org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task<T> implements Callable<T> {
    Task bindingTask, boundTask;
    private double boundTaskProgressStart;
    private double boundTaskProgressDelta;

    private final AtomicBoolean executing = new AtomicBoolean();
    private volatile boolean interrupted;

    private List<TaskListener> listenerList = new ArrayList<TaskListener>();
    private TaskListener[] listenersArray;

    private final String name;
    private final String logPrefix;

    public Task(String name) {
        this.name = name;
        logPrefix = "["+ getClass().getSimpleName() +":" + name + ']';
    }

    public final String getName() {
        return name;
    }

    public final void addListener(TaskListener listener) {
        U.requireNotNull(listener, "listener");
        if (listenersArray != null) {
            throw new IllegalStateException();
        }
        listenerList.add(listener);
    }

    public final Task getBindingTask() {
        return bindingTask;
    }

    public final void interrupt() {
        log("interrupted");
        this.interrupted = true;
        if(bindingTask != null) {
            bindingTask.interrupt();
        }
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
            return execute();
        } catch(InterruptedException inEx) {
            interrupted = true;
            throw inEx;
        } catch (Exception ex) {
            throw e = ex;
        } finally {
            if(interrupted) {
                log("interruption confirmed");
                updateProgress(-1.);
                for (TaskListener listener : listenersArray) {
                    listener.onTaskInterrupted(this);
                }
            } else {
                if (e == null) {
                    updateProgress(1.);
                    log("Done!");
                    for (TaskListener listener : listenersArray) {
                        listener.onTaskSucceeded(this);
                    }
                } else {
                    log("Failed:", e);
                    for (TaskListener listener : listenersArray) {
                        listener.onTaskErrored(this, e);
                    }
                }
            }
            listenerList.clear();
            listenersArray = null;
            executing.set(false);

            unbind();
        }
    }

    private void unbind() throws Exception {
        bindTo(null, 0., 0.);
    }

    protected final <V> V bindTo(Task<V> task, double startProgress, double stopProgress) throws Exception {
        if(startProgress > stopProgress) {
            throw new IllegalArgumentException();
        }

        if (listenersArray != null) {
            for (TaskListener listener : listenersArray) {
                listener.onTaskBound(this, task);
            }
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
        if(percentage < 0. || percentage > 1.) {
            percentage = -1.;
        }

        progress = percentage;

        if (listenersArray != null) {
            for (TaskListener listener : listenersArray) {
                listener.onTaskUpdated(this, percentage);
            }
        }

        if (boundTask != null) {
            boundTask.updateProgress(percentage == -1. ? boundTaskProgressStart : boundTaskProgressStart + percentage * boundTaskProgressDelta);
        }
    }

    public final boolean isExecuting() {
        return executing.get();
    }

    protected final void checkInterrupted() throws TaskInterruptedException {
        if(interrupted) {
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
