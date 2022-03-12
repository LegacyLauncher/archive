package ru.turikhay.tlauncher.bootstrap.task;

public class TaskListenerAdapter implements TaskListener {
    @Override
    public void onTaskStarted(Task task) {
    }

    @Override
    public void onTaskUpdated(Task task, double percentage) {
    }

    @Override
    public void onTaskBound(Task task, Task boundTo) {
    }

    @Override
    public void onTaskInterrupted(Task task) {
    }

    @Override
    public void onTaskSucceeded(Task task) {
    }

    @Override
    public void onTaskErrored(Task task, Exception e) {
    }
}
