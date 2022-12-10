package ru.turikhay.util.sysinfo;

import java.util.concurrent.CompletableFuture;

public interface SystemInfoReporter {
    void queueReport();
    CompletableFuture<SystemInfo> getReport();
}
