package net.legacylauncher.managers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PromotedStoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotedStoreManager.class);

    private final Lazy<CompletableFuture<Info>> info = Lazy.of(() ->
            AsyncThread.completableTimeout(10, TimeUnit.SECONDS, () ->
                    U.getGson().fromJson(
                            EHttpClient.toString(
                                    Request.get("https://api.llaun.ch/v1/promotedStore")
                                            .addHeader(HttpHeaders.ACCEPT, "application/json")
                            ),
                            Info.class
                    )
            ).whenComplete((r, t) -> {
                if (t != null) {
                    LOGGER.info("Promoted store info is not available: {}", t.toString());
                } else {
                    LOGGER.info("Promoted store info: {}", r);
                }
            })
    );

    public CompletableFuture<Info> requestOrGetInfo() {
        return info.get();
    }

    public Optional<Info> getInfoNow() {
        return info.valueIfInitialized().map(f -> {
            try {
                return f.getNow(null);
            } catch (RuntimeException ignored) {
                return null;
            }
        });
    }

    @EqualsAndHashCode
    @ToString
    @Getter
    public static class Info {
        private String id;
        private String url;
        private Map<String, String> text;

        public Info() {
        }

        public Info(String id, String url) {
            this.id = id;
            this.url = url;
        }
    }
}
