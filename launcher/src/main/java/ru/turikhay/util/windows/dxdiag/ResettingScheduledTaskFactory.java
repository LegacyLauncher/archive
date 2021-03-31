package ru.turikhay.util.windows.dxdiag;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Фабрика, имеющая выключатель {@link #mustReset}.
 * Если нас попросили создать новый таск, создаём.
 * Если нет, отдаём старый вне зависимости от его результата.
 */
class ResettingScheduledTaskFactory extends FilteringScheduledTaskFactory {
    private final AtomicBoolean mustReset = new AtomicBoolean();

    public ResettingScheduledTaskFactory(IScheduledTaskFactory delegateTask) {
        super(delegateTask);
    }

    public ResettingScheduledTaskFactory(TaskFactory taskFactory) {
        this(new SuccessiveScheduledTaskFactory(taskFactory));
    }

    public void mustReset() {
        mustReset.set(true);
    }

    @Override
    protected boolean mustRequestNewTask(Future<DxDiagReport> task) {
        return mustReset.getAndSet(false);
    }
}
