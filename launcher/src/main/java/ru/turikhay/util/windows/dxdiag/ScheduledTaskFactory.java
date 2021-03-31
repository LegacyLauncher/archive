package ru.turikhay.util.windows.dxdiag;

import java.util.concurrent.Future;

class ScheduledTaskFactory implements IScheduledTaskFactory {
    private final TaskFactory taskFactory;
    private final TaskScheduler taskScheduler;

    public ScheduledTaskFactory(TaskFactory taskFactory, TaskScheduler taskScheduler) {
        this.taskFactory = taskFactory;
        this.taskScheduler = taskScheduler;
    }

    public ScheduledTaskFactory(TaskFactory taskFactory) {
        this(taskFactory, new TaskScheduler());
    }

    @Override
    public Future<DxDiagReport> getScheduledTask() {
        return taskScheduler.scheduleTask(taskFactory.createTask());
    }
}
