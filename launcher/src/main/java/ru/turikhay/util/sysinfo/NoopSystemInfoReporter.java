package ru.turikhay.util.sysinfo;

import java.util.concurrent.CompletableFuture;

public class NoopSystemInfoReporter implements SystemInfoReporter {
    @Override
    public void queueReport() {
    }

    @Override
    public CompletableFuture<SystemInfo> getReport() {
        return CompletableFuture.completedFuture(null);
    }
}
