package ru.turikhay.tlauncher.bootstrap.task;

public interface TaskListener {
    void onTaskStarted(Task task);

    void onTaskUpdated(Task task, double percentage);

    void onTaskBound(Task task, Task boundTo);

    void onTaskInterrupted(Task task);

    void onTaskSucceeded(Task task);

    void onTaskErrored(Task task, Exception e);
}
