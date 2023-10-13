package net.legacylauncher.util.windows.dxdiag;

import java.util.concurrent.Future;

interface IScheduledTaskFactory {
    Future<DxDiagReport> getScheduledTask();
}
