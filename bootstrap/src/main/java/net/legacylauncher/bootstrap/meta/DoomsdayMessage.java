package net.legacylauncher.bootstrap.meta;

import com.google.gson.Gson;
import net.legacylauncher.afterlife.DoomsdayMessageV1;
import net.legacylauncher.util.shared.FutureUtils;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static net.legacylauncher.afterlife.DoomsdayMessageV1.URL_LIST;

public class DoomsdayMessage {
    public static DoomsdayMessageV1 requestSyncOrNull() {
        try {
            return makeRequest();
        } catch (Throwable t) {
            return null;
        }
    }

    private static DoomsdayMessageV1 makeRequest() throws Exception {
        Gson gson = new Gson();
        ExecutorService executor = Executors.newFixedThreadPool(
                URL_LIST.size(),
                r -> new Thread(r, "DoomsdayMessage")
        );
        try {
            return FutureUtils.fastestOf(
                    URL_LIST.stream()
                            .map(url -> makeSingleRequest(url, gson, executor))
                            .collect(Collectors.toList())
            ).get();
        } finally {
            executor.shutdown();
        }
    }

    private static CompletableFuture<DoomsdayMessageV1> makeSingleRequest(String _url, Gson gson, Executor executor) {
        return FutureUtils.supplyInterruptible(() -> {
            URL url = new URL(_url);
            try (InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, DoomsdayMessageV1.class).validate();
            }
        }, executor);
    }
}
