package ru.turikhay.tlauncher.bootstrap.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.turikhay.tlauncher.bootstrap.exception.FatalExceptionType;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskListener;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.util.Objects;

public final class HeadlessInterface implements IInterface {
    private static Gson gson;
    private final TaskListener<Object> listener;

    public HeadlessInterface() {
        listener = new TaskListener<Object>() {
            @Override
            public void onTaskStarted(Task<?> task) {
                printEvent(new TaskEvent(task));
            }

            @Override
            public void onTaskUpdated(Task<?> task, double percentage) {
                printEvent(new TaskEvent(task));
                if (percentage == 1.) {
                    onTaskSucceeded(task);
                }
            }

            @Override
            public void onTaskSucceeded(Task<?> task) {
                printEvent(new TaskEvent(task));
                dispose();
            }
        };
    }

    private Task<?> bindingTask;

    @Override
    public void bindToTask(Task<?> task) {
        if (this.bindingTask != null && this.bindingTask.isExecuting()) {
            throw new IllegalStateException();
        }

        this.bindingTask = task;
        if (this.bindingTask != null) {
            this.bindingTask.addListener(listener);
        }
    }

    @Override
    public void dispose() {
        printEvent(new DisposeEvent());
    }

    private static void printEvent(Event event) {
        U.log("[Hi]", gson().toJson(Objects.requireNonNull(event, "event")));
    }

    private static Gson gson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .create();
        }
        return gson;
    }

    private static void printAlert(EventAlertType eventType, String message, Object textarea) {
        printEvent(new AlertEvent(eventType, message, textarea));
    }

    static void printError(String message, Object textarea) {
        printAlert(EventAlertType.ERROR, message, textarea);
    }

    static void printWarning(String message, Object textarea) {
        printAlert(EventAlertType.WARNING, message, textarea);
    }

    public static void printVersion(String bootstrapVersion, String launcherVersion) {
        printEvent(new VersionEvent(bootstrapVersion, launcherVersion));
    }

    static void printFatalException(FatalExceptionType type) {
        printEvent(new FatalExceptionEvent(type));
    }

    private enum EventType {
        VERSION, TASK, ALERT, FATAL_EXCEPTION, DISPOSE
    }

    private enum EventAlertType {
        WARNING, ERROR
    }

    private static class Event {
        public final EventType type;

        Event(EventType type) {
            this.type = Objects.requireNonNull(type, "type");
        }
    }

    private static class VersionEvent extends Event {
        public final String bootstrapVersion, launcherVersion;

        VersionEvent(String bootstrapVersion, String launcherVersion) {
            super(EventType.VERSION);
            this.bootstrapVersion = Objects.requireNonNull(bootstrapVersion, "bootstrapVersion");
            this.launcherVersion = launcherVersion;
        }
    }

    private static class TaskEvent extends Event {
        public final String taskName, localizedTaskName;
        public final double progress;

        TaskEvent(Task<?> task) {
            super(EventType.TASK);
            Task<?> childTask = UserInterface.getChildTask(task, UserInterface.TASK_DEPTH);
            this.taskName = childTask.getName();
            this.localizedTaskName = UserInterface.getLocalizedTaskName(childTask);
            this.progress = task.getProgress();
        }
    }

    private static class AlertEvent extends Event {
        public final EventAlertType alertType;
        public final String message, appendMessage;

        public AlertEvent(EventAlertType type, String message, Object textarea) {
            super(EventType.ALERT);
            this.alertType = type;
            this.message = message;
            this.appendMessage = textarea instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) textarea) : textarea == null ? null : String.valueOf(textarea);
        }
    }

    private static class FatalExceptionEvent extends Event {
        public final FatalExceptionType exceptionType;

        FatalExceptionEvent(FatalExceptionType exceptionType) {
            super(EventType.FATAL_EXCEPTION);
            this.exceptionType = exceptionType;
        }
    }

    private static class DisposeEvent extends Event {
        public DisposeEvent() {
            super(EventType.DISPOSE);
        }
    }
}
