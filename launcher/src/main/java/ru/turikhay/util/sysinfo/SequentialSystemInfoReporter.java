package ru.turikhay.util.sysinfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SequentialSystemInfoReporter implements SystemInfoReporter {
    private static final Logger LOGGER = LogManager.getLogger(SequentialSystemInfoReporter.class);

    private final List<SystemInfoReporter> list;

    public SequentialSystemInfoReporter(SystemInfoReporter... list) {
        if (list.length == 0) {
            throw new IllegalArgumentException();
        }
        this.list = Arrays.asList(list);
    }

    @Override
    public void queueReport() {
        list.get(0).queueReport();
    }

    @Override
    public CompletableFuture<SystemInfo> getReport() {
        CompletableFuture<SystemInfo> r = new CompletableFuture<>();
        r.complete(null);
        for (SystemInfoReporter printer : list) {
            r = r.handle((result, ex) -> {
                if (ex != null) {
                    LOGGER.error("Couldn't query system info", ex);
                    return printer.getReport();
                } else if (result == null) {
                    // empty result
                    return printer.getReport();
                }
                return CompletableFuture.completedFuture(result);
            }).thenCompose(Function.identity());
        }
        return r;
    }

    @Override
    public String toString() {
        return "SequentialSystemInfoReporter{" +
                "list=" + list +
                '}';
    }
}
