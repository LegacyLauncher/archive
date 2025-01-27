package ru.turikhay.tlauncher.managers;

import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.util.EHttpClient;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PromotedStoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotedStoreManager.class);

    private final Lazy<CompletableFuture<Info>> info = Lazy.of(() -> {
        CompletableFuture<Info> info = AsyncThread.completableFuture(() ->
                U.getGson().fromJson(
                        EHttpClient.toString(
                                Request.Get("https://api.llaun.ch/v1/promotedStore")
                                        .addHeader(HttpHeaders.ACCEPT, "application/json")
                        ),
                        Info.class
                )
        );
        AsyncThread.after(10, TimeUnit.SECONDS, () -> info.completeExceptionally(new TimeoutException()));
        return info.whenComplete((r, t) -> {
            if (t != null) {
                LOGGER.info("Promoted store info is not available: {}", t.toString());
            } else {
                LOGGER.info("Promoted store info: {}", r);
            }
        });
    });

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

    public static class Info {
        private String id;
        private String url;
        private Map<String, String> text;

        public Info() {
        }

        public Info(String id, String url, Map<String, String> text) {
            this.id = id;
            this.url = url;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getText() {
            return text;
        }

        @Override
        public String toString() {
            return "ClassName{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", text=" + (text != null ? text.toString() : "null") +
                    '}';
        }
    }
}
