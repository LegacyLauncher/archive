package net.legacylauncher.util.sysinfo;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
public class SequentialSystemInfoReporter implements SystemInfoReporter {
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
                    log.error("Couldn't query system info", ex);
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
