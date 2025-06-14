package net.legacylauncher.stats;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.BuildConfig;
import net.legacylauncher.minecraft.PromotedServerAddStatus;
import net.legacylauncher.minecraft.Server;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.notice.Notice;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.U;
import net.legacylauncher.util.ua.LauncherUserAgent;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

@Slf4j
public final class Stats {
    private static final URL STATS_BASE = Http.constantURL("https://stats.llaun.ch/v2");
    private static final ExecutorService service = Executors.newCachedThreadPool();
    private static boolean allow = false;
    private static String lastResult;

    public static void setAllowed(boolean allowed) {
        allow = allowed;
    }

    public static void beacon() {
        submitDenunciation(newAction("beacon"));
    }

    public static void minecraftLaunched(Account<?> account, CompleteVersion version, Server server, int serverId, PromotedServerAddStatus promotionStatus) {
        Stats.Args args = newAction("mc_launched")
                .add("mc_version", version.getID())
                .add("username", account.getDisplayName())
                .add("account_type", account.getType().toString())
                .add("promotion_status", promotionStatus.toString());
        if (server != null) {
            args.add("server", server.getFullAddress());
        }
        if (serverId > 0) {
            args.add("server_id", String.valueOf(serverId));
        }

        submitDenunciation(args);
    }

    public static void noticeViewed(Notice notice) {
    }

    public static void noticeListViewed(List<Notice> list) {
    }

    public static void noticeHiddenByUser(Notice notice) {
    }

    public static void noticeShownByUser(Notice notice) {
    }

    public static void noticeStatusUpdated(boolean enabled) {
    }

    public static void noticeSceneShown() {
    }

    public static void feedbackStarted() {
    }

    public static void feedbackRefused(boolean returnBack) {
    }

    public static void accountCreation(String type, String strategy, String step, boolean success) {
    }

    public static Future<?> reportSessionDuration(long sessionStartTimeMillis) {
        long durationSeconds = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000L;
        return submitDenunciation(newAction("session_duration").add("duration", String.valueOf(durationSeconds)));
    }

    public static void submitNoticeStatus(boolean enabled) {
    }

    public static void showInterestInBuying(String promotedStoreId) {
        submitDenunciation(newAction("interested_in_buying").add("promoted_store_id", promotedStoreId));
    }

    public static void jarscannedCompleted(long seconds) {
        submitDenunciation(newAction("jarscanner_completed").add("seconds", String.valueOf(seconds)));
    }

    public static void jarscannedDetected(String name, String entry, String sha256) {
        submitDenunciation(newAction("jarscanner_detected").add("file_name", name).add("entry", entry).add("sha256", sha256));
    }

    public static void fractureiserTraceDetected() {
        submitDenunciation(newAction("fractureiser_detected"));
    }

    private static Stats.Args newAction(String name) {
        return new Stats.Args()
                .add("client", LegacyLauncher.getInstance().getSettings().getClient().toString())
                .add("version", String.valueOf(LegacyLauncher.getVersion()))
                .add("bootstrap", LegacyLauncher.getInstance().getBootstrapVersion())
                .add("brand", BuildConfig.SHORT_BRAND)
                .add("os", OS.CURRENT.getName())
                .add("locale", LegacyLauncher.getInstance().getLang().getLocale().toString())
                .add("action", name);
    }

    private static Future<?> submitDenunciation(final Stats.Args args) {
        if (allow) {
            return service.submit((Callable<Void>) () -> {
                String result = Stats.performPostRequest(Stats.STATS_BASE, Stats.toRequest(args));

                if (StringUtils.isNotEmpty(result)) {
                    lastResult = result;
                    for (StatsListener l : listeners) {
                        l.onInvalidSubmit(result);
                    }
                }

                return null;
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    private static String toRequest(Stats.Args args) {
        StringBuilder b = new StringBuilder();

        for (Entry<String, String> entry : args.map.entrySet()) {
            b.append('&').append(Http.encode(entry.getKey())).append('=').append(Http.encode(entry.getValue()));
        }

        return b.substring(1);
    }

    private static HttpURLConnection createUrlConnection(URL url) throws IOException {
        Objects.requireNonNull(url);
        log.trace("Opening connection to {}", url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());
        connection.setUseCaches(false);
        LauncherUserAgent.set(connection);
        return connection;
    }

    public static String performPostRequest(URL url, String request) throws IOException {
        Objects.requireNonNull(url);
        Objects.requireNonNull(request);
        HttpURLConnection connection = createUrlConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        log.trace("Writing data to {}", url);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(request);
        }
        log.trace("Reading data from {}", url);
        try (InputStream inputStream = connection.getInputStream()) {
            String e = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            log.debug("{} responded with {}: {}", url, connection.getResponseCode(), e);
            return e;
        } catch (IOException e) {
            log.warn("Stats request failed: {}", e.toString());
            throw e;
        }
    }

    private static class Args {
        private final LinkedHashMap<String, String> map;

        private Args() {
            map = new LinkedHashMap<>();
        }

        public Stats.Args add(String key, String value) {
            map.put(key, value);
            return this;
        }
    }

    private static final List<StatsListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public static void addListener(StatsListener listener) {
        listeners.add(listener);

        if (lastResult != null) {
            listener.onInvalidSubmit(lastResult);
        }
    }

    public interface StatsListener {
        void onInvalidSubmit(String message);
    }

    private Stats() {
    }
}
