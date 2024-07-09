package net.legacylauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.legacylauncher.ui.notice.Notice;
import net.legacylauncher.ui.notice.NoticeDeserializer;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PersonalNoticeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalNoticeManager.class);

    private CompletableFuture<Payload> payload;

    private static String e(Object value) {
        try {
            return URLEncoder.encode(String.valueOf(value), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public void queueRequest(UUID client, String version, String bootstrapName, String bootstrapVersion, Locale locale) {
        if (payload != null) {
            throw new IllegalStateException();
        }
        payload = AsyncThread.completableTimeout(10, TimeUnit.SECONDS, () -> {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Notice.class, new NoticeDeserializer())
                    .create();
            String url = "https://api.llaun.ch/v1/notices/" + client +
                    "?version=" + e(version) +
                    "&bootstrap_name=" + e(bootstrapName) +
                    "&bootstrap_version=" + e(bootstrapVersion) +
                    "&locale=" + e(locale);
            return gson.fromJson(
                    EHttpClient.toString(Request.get(url)
                            .addHeader(HttpHeaders.ACCEPT, "application/json")),
                    Payload.class
            );
        }).whenComplete((r, t) -> {
            if (t != null) {
                LOGGER.info("Personal notices are not available: {}", t.toString());
            } else {
                LOGGER.info("Personal notices: {}", r);
            }
        });
    }

    public CompletableFuture<Payload> getRequestOnce() {
        if (payload == null) {
            throw new IllegalStateException();
        }
        try {
            return payload;
        } finally {
            payload = null;
        }
    }

    public static class Payload {
        private List<Notice> notices;

        public Payload(List<Notice> notices) {
            this.notices = notices;
        }

        public Payload() {
        }

        public List<Notice> getNotices() {
            return notices;
        }

        @Override
        public String toString() {
            return "Payload{" +
                    "notices=" + notices +
                    '}';
        }
    }
}
