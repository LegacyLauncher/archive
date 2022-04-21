package ru.turikhay.tlauncher.bootstrap.task;

public class TaskInterruptedException extends TaskException {
    TaskInterruptedException(Task<?> task) {
        super(task);
    }
}
