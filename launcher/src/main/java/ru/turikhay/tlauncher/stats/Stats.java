package ru.turikhay.tlauncher.stats;

import net.minecraft.launcher.Http;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.PromotedServerAddStatus;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public final class Stats {
    private static final Logger LOGGER = LogManager.getLogger(Stats.class);

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
        Stats.Args args = newAction("mc_launched").add("mc_version", version.getID()).add("account_type", account.getType().toString()).add("promotion_status", promotionStatus.toString());
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

    public static void showInterestInBuying(boolean promotedStore) {
        submitDenunciation(newAction("interested_in_buying").add("promoted_store", String.valueOf(promotedStore)));
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
                .add("client", TLauncher.getInstance().getSettings().getClient().toString())
                .add("version", String.valueOf(TLauncher.getVersion()))
                .add("bootstrap", TLauncher.getInstance().getBootstrapVersion())
                .add("brand", TLauncher.getShortBrand())
                .add("os", OS.CURRENT.getName())
                .add("locale", TLauncher.getInstance().getLang().getLocale().toString())
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
        LOGGER.trace("Opening connection to {}", url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());
        connection.setUseCaches(false);
        return connection;
    }

    public static String performPostRequest(URL url, String request) throws IOException {
        Objects.requireNonNull(url);
        Objects.requireNonNull(request);
        HttpURLConnection connection = createUrlConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        LOGGER.trace("Writing data to {}", url);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(request);
        }
        LOGGER.trace("Reading data from {}", url);
        try (InputStream inputStream = connection.getInputStream()) {
            String e = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            LOGGER.debug("{} responded with {}: {}", url, connection.getResponseCode(), e);
            return e;
        } catch (IOException e) {
            LOGGER.warn("Stats request failed: {}", e.toString());
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
