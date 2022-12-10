package ru.turikhay.util.sysinfo;

import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.windows.dxdiag.DxDiag;
import ru.turikhay.util.windows.dxdiag.DxDiagReport;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DxDiagSystemInfoReporter implements SystemInfoReporter {
    @Override
    public void queueReport() {
        DxDiag.getInstance().queueTask();
    }

    @Override
    public CompletableFuture<SystemInfo> getReport() {
        CompletableFuture<SystemInfo> f = new CompletableFuture<>();
        AsyncThread.execute(() -> checkIfDxDiagComplete(f));
        return f;
    }

    private void checkIfDxDiagComplete(CompletableFuture<SystemInfo> f) {
        Future<DxDiagReport> task = DxDiag.getInstanceTask();
        if (task.isDone()) {
            final DxDiagReport result;
            try {
                result = task.get();
            } catch (InterruptedException | ExecutionException e) {
                f.completeExceptionally(e);
                return;
            }
            AsyncThread.execute(() -> f.complete(convertDxDiagReport(result))); // completeAsync
            return;
        }
        AsyncThread.afterSeconds(1, () -> checkIfDxDiagComplete(f));
    }

    private SystemInfo convertDxDiagReport(DxDiagReport report) {
        return new SystemInfo(
                Arrays.asList(report.getSysInfo().toString().split("\n")),
                report.getSysInfo().is64Bit(),
                report.getDisplayDevices().stream().map(d ->
                        new GraphicsCard(d.getCardName(), d.getManufacturer(), d.getDriverVersion())
                ).collect(Collectors.toList())
        );
    }
}
