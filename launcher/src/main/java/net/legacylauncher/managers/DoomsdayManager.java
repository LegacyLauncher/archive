package net.legacylauncher.managers;

import net.legacylauncher.afterlife.DoomsdayMessageV1;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.shared.FutureUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DoomsdayManager {
    private static final Lazy<CompletableFuture<DoomsdayMessageV1>> LAZY = Lazy.of(() -> {
        @SuppressWarnings("resource") // will be closed async-ly
        CloseableHttpClient client = EHttpClient.createRepeatable();
        CompletableFuture<DoomsdayMessageV1> result = FutureUtils.fastestOf(
                DoomsdayMessageV1.URL_LIST
                        .stream()
                        .map(Request::get)
                        .map((request) -> AsyncThread.supplyInterruptible(() ->
                                U.getGson().fromJson(EHttpClient.toString(client, request), DoomsdayMessageV1.class).validate()
                        ))
                        .collect(Collectors.toList())
        );
        AsyncThread.afterSeconds(15, () -> result.completeExceptionally(new TimeoutException()));
        result.whenComplete((__, t) -> IOUtils.closeQuietly(client));
        return result;
    });

    private DoomsdayManager() {
    }

    public static CompletableFuture<DoomsdayMessageV1> queryMessage() {
        return LAZY.get();
    }
}
