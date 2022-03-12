package ru.turikhay.util.windows.dxdiag;

import java.util.concurrent.Future;

/**
 * «Фильтрующая» фабрика. Запоминает свой последний созданный таск.
 * Логика создания нового таска отдаёт на откуп дочернему классу.
 */
abstract class FilteringScheduledTaskFactory implements IScheduledTaskFactory {
    private final IScheduledTaskFactory delegateTask;

    private volatile Future<DxDiagReport> task;

    public FilteringScheduledTaskFactory(IScheduledTaskFactory delegateTask) {
        this.delegateTask = delegateTask;
    }

    @Override
    public synchronized Future<DxDiagReport> getScheduledTask() {
        if (task == null || mustRequestNewTask(task)) {
            task = delegateTask.getScheduledTask();
        }
        return task;
    }

    protected abstract boolean mustRequestNewTask(Future<DxDiagReport> task);
}
