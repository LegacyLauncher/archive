package net.legacylauncher.managers;

import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PromotedStoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotedStoreManager.class);

    private final Lazy<CompletableFuture<Info>> info = Lazy.of(() ->
            AsyncThread.completableTimeout(10, TimeUnit.SECONDS, () ->
                    U.getGson().fromJson(
                            EHttpClient.toString(
                                    Request.Get("https://api.llaun.ch/v1/promotedStore")
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

    public static class Info {
        private String id;
        private String url;
        private String price;

        public Info() {
        }

        public Info(String id, String url) {
            this.id = id;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public String getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Info)) return false;
            Info info = (Info) o;
            return Objects.equals(id, info.id) && Objects.equals(url, info.url) && Objects.equals(price, info.price);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, url, price);
        }

        @Override
        public String toString() {
            return "Info{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", price='" + price + '\'' +
                    '}';
        }
    }
}
