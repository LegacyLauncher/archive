package ru.turikhay.util.windows.dxdiag;

import ru.turikhay.util.async.AsyncThread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class TaskScheduler {
    public Future<DxDiagReport> scheduleTask(Callable<DxDiagReport> task) {
        return AsyncThread.future(task);
    }
}
