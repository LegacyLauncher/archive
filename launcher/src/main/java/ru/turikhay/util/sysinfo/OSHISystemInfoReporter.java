package ru.turikhay.util.sysinfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.HardwareAbstractionLayer;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.async.AsyncThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OSHISystemInfoReporter implements SystemInfoReporter {
    private static final Logger LOGGER = LogManager.getLogger(OSHISystemInfoReporter.class);

    private final CompletableFuture<oshi.SystemInfo> oshiSystemInfoFuture = new CompletableFuture<>();

    @Override
    public void queueReport() {
        AsyncThread.execute(() -> oshiSystemInfoFuture.complete(new oshi.SystemInfo()));
        AsyncThread.afterSeconds(60, () ->
                oshiSystemInfoFuture.completeExceptionally(
                        new TimeoutException("creating oshi system info is taking too long")
                )
        );
    }

    @Override
    public CompletableFuture<SystemInfo> getReport() {
        return oshiSystemInfoFuture.thenApplyAsync((si) -> {
            HardwareAbstractionLayer hardware = si.getHardware();
            ArrayList<String> lines = new ArrayList<>();
            lines.add("---");
            lines.add("OS: ");
            addLines(si.getOperatingSystem(), lines);
            lines.add("---");
            lines.add("Device:");
            addLines(hardware.getComputerSystem(), lines);
            lines.add("---");
            lines.add("CPU:");
            addLines(hardware.getProcessor(), lines);
            lines.add("---");
            lines.add("RAM:");
            addLines(hardware.getMemory(), lines);
            List<oshi.hardware.GraphicsCard> graphicsCards = hardware.getGraphicsCards();
            for (int i = 0; i < graphicsCards.size(); i++) {
                lines.add("---");
                lines.add("GPU#" + i + ":");
                addLines(graphicsCards.get(i), lines);
            }
            lines.add("---");
            return new SystemInfo(
                    lines,
                    si.getOperatingSystem().getBitness() == 64,
                    graphicsCards.stream().map(g ->
                            new GraphicsCard(g.getName(), g.getVendor(), g.getVersionInfo())
                    ).collect(Collectors.toList())
            );
        }, AsyncThread.SHARED_SERVICE);
    }

    private static void addLines(Object o, ArrayList<String> lines) {
        if (o == null) {
            lines.add("<NULL>");
            return;
        }
        lines.addAll(Arrays.asList(o.toString().split("\n")));
    }


    private static final Lazy<Boolean> IS_AVAILABLE = Lazy.of(() -> {
        try {
            Class.forName("oshi.SystemInfo");
        } catch (ClassNotFoundException | Error err) {
            LOGGER.warn("OSHI system info reporter is not available: {}", err.toString());
            return false;
        }
        return true;
    });

    public static Optional<SystemInfoReporter> createIfAvailable() {
        return IS_AVAILABLE.get() ? Optional.of(new OSHISystemInfoReporter()) : Optional.empty();
    }
}
