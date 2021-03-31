package ru.turikhay.util.windows.dxdiag;

import ru.turikhay.util.OS;

import java.util.concurrent.Future;

public class DxDiag implements IScheduledTaskFactory {
    private final ResettingScheduledTaskFactory taskFactory;

    public DxDiag() {
        /*
            Вложенные фабрики это зло, говорили они. На самом деле это непревзойденная гибкость.
            Вкратце:
            DxDiag#getScheduledTask отдаёт TimeLimitedTask, который сгнивает через две минуты после
            запуска, если основной DxDiagTask не успевает к тому времени закончить.
            Если DxDiagTask не заканчивает работу в течение двух минут или сам завершает работу с ошибкой,
            все попытки получить DxDiagReport кончатся неудачей. В этом случае нам нужно вызвать queueTask():
            ResettingScheduledTaskFactory ресетнется и, таким образом, запросит новый TimeLimitedTask
            (и, по цепочке, новый DxDiagTask, если тот завершился с ошибкой).
         */
        this(
                new ResettingScheduledTaskFactory(
                        new TimeLimitedTask.Factory(
                                new DxDiagTask.Factory()
                        )
                )
        );
    }

    DxDiag(ResettingScheduledTaskFactory supplier) {
        this.taskFactory = supplier;
    }

    public void queueTask(boolean mayResetNextTime) {
        if (mayResetNextTime) {
            taskFactory.mustReset();
        }
        getScheduledTask(); // just trigger
    }

    public void queueTask() {
        queueTask(true);
    }

    @Override
    public Future<DxDiagReport> getScheduledTask() {
        return taskFactory.getScheduledTask();
    }

    private static DxDiag instance;

    public static synchronized DxDiag getInstance() {
        if (instance == null) {
            instance = new DxDiag();
        }
        return instance;
    }

    public static synchronized Future<DxDiagReport> getInstanceTask() {
        return getInstance().getScheduledTask();
    }

    public static boolean canExecute() {
        return OS.WINDOWS.isCurrent();
    }
}
