package ru.turikhay.tlauncher.bootstrap.task;

public interface TaskListener<T> {
    default void onTaskStarted(Task<? extends T> task) {
    }

    default void onTaskUpdated(Task<? extends T> task, double percentage) {
    }

    default void onTaskBound(Task<? extends T> task, Task<?> boundTo) {
    }

    default void onTaskInterrupted(Task<? extends T> task) {
    }

    default void onTaskSucceeded(Task<? extends T> task) {
    }

    default void onTaskErrored(Task<? extends T> task, Exception e) {
    }
}
