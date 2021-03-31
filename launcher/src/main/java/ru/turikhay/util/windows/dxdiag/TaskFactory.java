package ru.turikhay.util.windows.dxdiag;

import java.util.concurrent.Callable;

public interface TaskFactory {
    Callable<DxDiagReport> createTask();
}
