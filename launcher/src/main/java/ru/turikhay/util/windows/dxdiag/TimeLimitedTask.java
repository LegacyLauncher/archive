package ru.turikhay.util.windows.dxdiag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Таск, который не заставляет пользователя (очень долго) ждать результатов другого таска.
 * Если {@link #reportTask} идёт слишком долго, то просто бросаем {@link TimeoutException}
 * и продолжаем работу.
 * <p>
 * В этом таске время тайм-аутов всегда идёт относительно начала другого таска:
 * если кто-то вызовет {@link Future#get(long, TimeUnit)}, то мы не будем ждать каждый раз заново.
 * Максимум 2 минуты (по умолчанию).
 */
class TimeLimitedTask implements Callable<DxDiagReport> {
    private static final long DEFAULT_LIMIT_IN_SECONDS = 60 * 2;

    private static final Logger LOGGER = LogManager.getLogger(TimeLimitedTask.class);

    private final Future<DxDiagReport> reportTask;
    private final long timeout;
    private final TimeUnit timeUnit;

    TimeLimitedTask(Future<DxDiagReport> reportTask, long timeout, TimeUnit timeUnit) {
        this.reportTask = reportTask;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    TimeLimitedTask(Future<DxDiagReport> reportTask) {
        this(reportTask, DEFAULT_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public DxDiagReport call() throws Exception {
        try {
            return reportTask.get(timeout, timeUnit);
        } catch (TimeoutException timeoutException) {
            LOGGER.warn("DxDiag did not complete in {} seconds", timeUnit.toSeconds(timeout));
            throw timeoutException;
        }
    }

    public static class Factory implements TaskFactory {
        private final IScheduledTaskFactory scheduledTaskFactory;

        public Factory(IScheduledTaskFactory scheduledTaskFactory) {
            this.scheduledTaskFactory = scheduledTaskFactory;
        }

        public Factory(TaskFactory taskFactory) {
            this(new SuccessiveScheduledTaskFactory(taskFactory));
        }

        @Override
        public TimeLimitedTask createTask() {
            return new TimeLimitedTask(scheduledTaskFactory.getScheduledTask());
        }
    }
}
