package ru.turikhay.tlauncher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.Http;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.util.Compressor;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Updater {
    private final Gson gson = buildGson();
    private boolean refreshed;
    private Update update;
    private final List<UpdaterListener> listeners = Collections.synchronizedList(new ArrayList<UpdaterListener>());

    public boolean getRefreshed() {
        return refreshed;
    }

    public void setRefreshed(boolean refreshed) {
        this.refreshed = refreshed;
    }

    public Update getUpdate() {
        return update;
    }

    private Updater.SearchResult localTestUpdate() {
        URL url = getClass().getResource("default.json");
        if (url == null) {
            return null;
        } else {
            InputStreamReader reader = null;
            Updater.SearchSucceeded result;
            try {
                reader = new InputStreamReader(url.openStream());
                result = new Updater.SearchSucceeded(gson.fromJson(reader, Updater.UpdaterResponse.class));
                if (result.getResponse().getNotices().map().isEmpty()) {
                    throw new IllegalArgumentException("local notice list is empty, skipping");
                }
            } catch (Exception e) {
                log("Could not request update from local file:", e.toString());
                return null;
            } finally {
                U.close(reader);
            }
            return result;
        }
    }

    protected Updater.SearchResult findUpdate0() {
        SearchResult result = null;
        if (TLauncher.getInstance().getDebug()) {
            result = localTestUpdate();
            if (result != null) {
                log("Requested update from local file");
                return result;
            }
        }

        log("Requesting an update...");
        ArrayList errorList = new ArrayList();
        String get = "?version=" + Http.encode(String.valueOf(TLauncher.getVersion())) + "&brand=" + Http.encode(TLauncher.getBrand()) + "&client=" + Http.encode(TLauncher.getInstance().getSettings().getClient().toString()) + "&beta=" + Http.encode(String.valueOf(TLauncher.isBeta()));
        Iterator var5 = getUpdateUrlList().iterator();
        int attempt = 0;

        while (var5.hasNext()) {
            ++attempt;

            String updateUrl = (String) var5.next();
            long startTime = System.currentTimeMillis();
            log("Requesting from:", updateUrl);

            try {
                URL e = new URL(updateUrl + get);
                HttpURLConnection connection = Downloadable.setUp(e.openConnection(U.getProxy()), true);
                connection.setConnectTimeout(3000 * attempt);
                connection.setReadTimeout(3000 * attempt);
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

                InputStream in = uncompress(connection);
                result = new Updater.SearchSucceeded(gson.fromJson(new InputStreamReader(in, FileUtil.DEFAULT_CHARSET), UpdaterResponse.class));

                try {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

                    long refreshTime = result.getResponse().getRefreshTime(),
                            currentTimeGMT = calendar.getTimeInMillis() / 1000;

                    calendar.setTimeInMillis(refreshTime * 1000);
                    log("Next refresh time:", calendar.getTime());

                    if (refreshTime > 0 && currentTimeGMT > refreshTime)
                        throw new IOException("refresh time exceeded");
                } catch (IOException ioE) {
                    throw ioE;
                } catch (Exception ignored) {
                    log(ignored);
                }

            } catch (Exception var11) {
                log("Failed to request from:", updateUrl, var11);
                result = null;
                errorList.add(var11);
            }

            log("Request time:", Long.valueOf(System.currentTimeMillis() - startTime), "ms");
            if (result != null) {
                log("Succeed!");
                log("Latest version:", result.getResponse().getUpdate().getVersion() + (result.getResponse().getUpdate() == result.getResponse().beta ? " (beta)" : ""));
                break;
            }
        }

        return refreshed ? null : (result == null ? new SearchFailed(errorList) : result);
    }

    public Updater.SearchResult findUpdate() {
        try {
            Updater.SearchResult e = findUpdate0();
            dispatchResult(e);
            return e;
        } catch (Exception var2) {
            return null;
        }
    }

    public void asyncFindUpdate() {
        AsyncThread.execute(new Runnable() {
            public void run() {
                findUpdate();
            }
        });
    }

    public void addListener(UpdaterListener l) {
        listeners.add(l);
    }

    public void removeListener(UpdaterListener l) {
        listeners.remove(l);
    }

    public void dispatchResult(Updater.SearchResult result) {
        requireNotNull(result, "result");
        List var2;
        UpdaterListener l;
        Iterator var4;
        if (result instanceof Updater.SearchSucceeded) {
            Stats.setAllowed(result.getResponse().isStatsAllowed());
            var2 = listeners;
            synchronized (listeners) {
                var4 = listeners.iterator();

                while (var4.hasNext()) {
                    l = (UpdaterListener) var4.next();
                    l.onUpdaterSucceeded((Updater.SearchSucceeded) result);
                }
            }
        } else {
            if (!(result instanceof Updater.SearchFailed)) {
                throw new IllegalArgumentException("unknown result of " + result.getClass());
            }

            var2 = listeners;
            synchronized (listeners) {
                var4 = listeners.iterator();

                while (var4.hasNext()) {
                    l = (UpdaterListener) var4.next();
                    l.onUpdaterErrored((Updater.SearchFailed) result);
                }
            }
        }

    }

    protected void onUpdaterRequests() {
        List var1 = listeners;
        synchronized (listeners) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                UpdaterListener l = (UpdaterListener) var3.next();
                l.onUpdaterRequesting(this);
            }

        }
    }

    protected List<String> getUpdateUrlList() {
        return Arrays.asList(TLauncher.getUpdateRepos());
    }

    protected Gson buildGson() {
        return (new GsonBuilder()).registerTypeAdapter(Notices.class, new Notices.Deserializer()).registerTypeAdapter(Update.class, new Update.Deserializer()).create();
    }

    public Updater.SearchSucceeded newSucceeded(Updater.UpdaterResponse response) {
        return new Updater.SearchSucceeded(response);
    }

    protected void log(Object... o) {
        U.log("[Updater]", o);
    }

    private static <T> T requireNotNull(T obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name);
        } else {
            return obj;
        }
    }

    private InputStream uncompress(URLConnection connection) throws IOException {
        Compressor.init();

        InputStream in = connection.getInputStream();

        if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
            log("Data is compressed by the server with gzip");
            in = new GzipCompressorInputStream(in);
        } else {
            in = Compressor.uncompressMarked(in);

            if (in instanceof Compressor.CompressedInputStream) {
                log("Data is compressed by the script with", ((Compressor.CompressedInputStream) in).getCompressor().getName());
            } else {
                log("Data is not compressed at all");
            }
        }

        return in;
    }

    public class SearchFailed extends Updater.SearchResult {
        protected final List<Throwable> errorList = new ArrayList();

        public SearchFailed(List<Throwable> list) {
            super(null);
            Iterator var4 = list.iterator();

            while (var4.hasNext()) {
                Throwable t = (Throwable) var4.next();
                if (t == null) {
                    throw new NullPointerException();
                }
            }

            errorList.addAll(list);
        }

        public final List<Throwable> getCauseList() {
            return errorList;
        }

        public String toString() {
            return getClass().getSimpleName() + "{errors=" + errorList + "}";
        }
    }

    public abstract class SearchResult {
        protected final Updater.UpdaterResponse response;

        public SearchResult(Updater.UpdaterResponse response) {
            this.response = response;
        }

        public final Updater.UpdaterResponse getResponse() {
            return response;
        }

        public final Updater getUpdater() {
            return Updater.this;
        }

        public String toString() {
            return getClass().getSimpleName() + "{response=" + response + "}";
        }
    }

    public class SearchSucceeded extends Updater.SearchResult {
        public SearchSucceeded(Updater.UpdaterResponse response) {
            super(response);
        }
    }

    public final static class UpdaterResponse {
        private Update update, beta;
        private Notices notices, ads;
        private boolean allowStats, allowElyGlobally;
        private long refreshTime;

        public UpdaterResponse(Update update) {
            this.update = update;
        }

        public Update getUpdate() {
            return TLauncher.isBeta() && beta != null ? beta : update;
        }

        public Notices getNotices() {
            return notices == null ? ads : notices;
        }

        public boolean isStatsAllowed() {
            return allowStats;
        }

        public boolean isElyAllowedGlobally() {
            return allowElyGlobally;
        }

        public long getRefreshTime() {
            return refreshTime;
        }

        public String toString() {
            return "UpdaterResponse{update=" + getUpdate() + ", notices=" + getNotices() + "}";
        }
    }
}
