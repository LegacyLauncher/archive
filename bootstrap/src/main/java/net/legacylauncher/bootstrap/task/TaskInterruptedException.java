package net.legacylauncher.bootstrap.task;

public class TaskInterruptedException extends TaskException {
    TaskInterruptedException(Task<?> task) {
        super(task);
    }
}
