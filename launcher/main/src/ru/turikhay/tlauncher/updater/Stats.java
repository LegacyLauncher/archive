package ru.turikhay.tlauncher.updater;

import net.minecraft.launcher.Http;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Stats {
    private static final URL STATS_BASE = Http.constantURL("http://u.tlauncher.ru/stats/");
    private static final ExecutorService service = Executors.newCachedThreadPool();
    private static boolean allow = false;
    private static String lastResult;

    public static void setAllowed(boolean allowed) {
        allow = allowed;
    }

    public static void beacon() {
        submitDenunciation(newAction("beacon"));
    }

    public static void minecraftLaunched(Account account, CompleteVersion version, Server server) {
        Stats.Args args = newAction("mc_launched").add("mc_version", version.getID()).add("account_type", account.getType().toString());
        if (server != null) {
            args.add("server", server.getFullAddress());
        }

        submitDenunciation(args);
    }

    public static void noticeViewed(Notice notice) {
        submitDenunciation(newAction("notice_viewed").add("notice_id", String.valueOf(notice.getId())));
    }

    public static void noticeHiddenByUser(Notice notice) {
        submitDenunciation(newAction("notice_act_hidden").add("notice_id", String.valueOf(notice.getId())));
    }

    public static void noticeShownByUser(Notice notice) {
        submitDenunciation(newAction("notice_act_shown").add("notice_id", String.valueOf(notice.getId())));
    }

    private static Stats.Args newAction(String name) {
        return new Stats.Args()
                .add("client", TLauncher.getInstance().getSettings().getClient().toString())
                .add("version", String.valueOf(TLauncher.getVersion()))
                .add("brand", TLauncher.getBrand())
                .add("os", OS.CURRENT.getName())
                .add("locale", TLauncher.getInstance().getLang().getLocale().toString())
                .add("action", name);
    }

    private static void submitDenunciation(final Stats.Args args) {
        if (allow) {
            service.submit(new Callable() {
                public Void call() throws Exception {
                    String result = Stats.performGetRequest(Stats.STATS_BASE, Stats.toRequest(args));

                    if (StringUtils.isNotEmpty(result)) {
                        lastResult = result;
                        for (StatsListener l : listeners) {
                            l.onInvalidSubmit(result);
                        }
                    }

                    return null;
                }
            });
        }
    }

    private static String toRequest(Stats.Args args) {
        StringBuilder b = new StringBuilder();
        Iterator var3 = args.map.entrySet().iterator();

        while (var3.hasNext()) {
            Entry arg = (Entry) var3.next();
            b.append('&').append(Http.encode((String) arg.getKey())).append('=').append(Http.encode((String) arg.getValue()));
        }

        return b.substring(1);
    }

    private static HttpURLConnection createUrlConnection(URL url) throws IOException {
        Validate.notNull(url);
        debug("Opening connection to " + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());
        connection.setUseCaches(false);
        return connection;
    }

    public static String performGetRequest(URL url, String request) throws IOException {
        Validate.notNull(url);
        Validate.notNull(request);
        url = new URL(url.toString() + '?' + request);
        HttpURLConnection connection = createUrlConnection(url);
        debug("Reading data from " + url);
        InputStream inputStream = null;

        String var7;
        try {
            try {
                inputStream = connection.getInputStream();
                String e = IOUtils.toString(inputStream, Charsets.UTF_8);
                debug("Successful read, server response was " + connection.getResponseCode());
                debug("Response: " + e);
                var7 = e;
                return var7;
            } catch (IOException var10) {
                IOUtils.closeQuietly(inputStream);
                inputStream = connection.getErrorStream();
                if (inputStream == null) {
                    debug("Request failed", var10);
                    throw var10;
                }
            }

            debug("Reading error page from " + url);
            String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            var7 = result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return var7;
    }

    private static void debug(Object... o) {
        if (TLauncher.getInstance() == null || TLauncher.getInstance().isDebug()) {
            U.log("[Stats]", o);
        }
    }

    private static class Args {
        private final LinkedHashMap<String, String> map;

        private Args() {
            map = new LinkedHashMap();
        }

        public Stats.Args add(String key, String value) {
            if (map.containsKey(key)) {
                map.remove(key);
            }

            map.put(key, value);
            return this;
        }
    }

    private static final List<StatsListener> listeners = Collections.synchronizedList(new ArrayList<StatsListener>());

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
