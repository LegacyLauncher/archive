package ru.turikhay.tlauncher.bootstrap.task;

public class TaskException extends Exception {
    TaskException(Task<?> task) {
        super(getTaskName(task, null));
    }

    TaskException(Task<?> task, Throwable cause) {
        super(getTaskName(task, cause), cause);
    }

    private static String getTaskName(Task<?> task, Throwable cause) {
        if (task == null) {
            return cause == null ? "(null task, null cause)" : "(null task)";
        }

        Task<?> currentTask = getHighestTask(task);
        StringBuilder b = new StringBuilder();

        if (cause != null) {
            b.append(cause).append("\n");
        }

        b.append(currentTask.toString());

        while (currentTask.bindingTask != null) {
            b.append(" -> ").append(currentTask = currentTask.bindingTask);
        }

        return b.toString();
    }

    private static Task<?> getHighestTask(Task<?> task) {
        while (task.boundTask != null) {
            task = task.boundTask;
        }
        return task;
    }
}
