package ru.turikhay.util.windows.dxdiag;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Фабрика, которая запоминает успешный таск навсегда.
 * Если последний таск завершился неудачей, то запрашивает новый у родительской фабрики.
 */
class SuccessiveScheduledTaskFactory extends FilteringScheduledTaskFactory {
    public SuccessiveScheduledTaskFactory(IScheduledTaskFactory delegateTask) {
        super(delegateTask);
    }

    public SuccessiveScheduledTaskFactory(TaskFactory taskFactory) {
        this(new ScheduledTaskFactory(taskFactory));
    }

    @Override
    protected boolean mustRequestNewTask(Future<DxDiagReport> task) {
        return task.isDone() && isFailed(task);
    }

    static boolean isFailed(Future<DxDiagReport> scheduledTask) {
        try {
            scheduledTask.get();
        } catch (InterruptedException | ExecutionException e) {
            return true;
        }
        return false;
    }
}
