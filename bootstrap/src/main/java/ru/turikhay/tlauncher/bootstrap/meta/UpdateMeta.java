package ru.turikhay.tlauncher.bootstrap.meta;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.exception.ExceptionList;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.json.UpdateDeserializer;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.transport.SignedStream;
import ru.turikhay.tlauncher.bootstrap.util.Compressor;
import ru.turikhay.tlauncher.bootstrap.util.DataBuilder;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class UpdateMeta {
    private static final List<String> UPDATE_URL_LIST = new ArrayList<String>() {
        {
            add("http://cdn.turikhay.ru/tlauncher/%s/bootstrap.json.mgz.signed");
            Collections.addAll(this, U.shuffle(
                    "http://tlauncherrepo.com/%s/bootstrap.json.mgz.signed",
                    "http://u.tlauncher.ru/%s/bootstrap.json.mgz"
            ));
            Collections.addAll(this, U.shuffle(
                    "https://tlauncherrepo.com/%s/bootstrap.json",
                    "https://u.tlauncher.ru/%s/bootstrap.json",
                    "https://turikhay.ru/tlauncher/%s/bootstrap.json"
            ));
        }
    };
    static {
        UPDATE_URL_LIST.add("https://tlaun.ch/%s/bootstrap.json");
        UPDATE_URL_LIST.add("http://tlaun.ch/%s/bootstrap.json.mgz.signed");
        Compressor.init(); // init compressor
    }

    private static final int INITIAL_TIMEOUT = 1500, MAX_ATTEMPTS = 5;

    public static Task<UpdateMeta> fetchFor(final String brand) throws ExceptionList {
        U.requireNotNull(brand,  "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                log("Requesting update for: " + brand);

                Gson gson = createGson();
                List<Exception> eList = new ArrayList<Exception>();

                for(int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                    for (int i = 0; i < UPDATE_URL_LIST.size(); i++) {
                        checkInterrupted();
                        updateProgress((i+1) / UPDATE_URL_LIST.size());

                        String _url = null;
                        long time = System.currentTimeMillis();

                        InputStream stream = null;
                        try {
                            _url = String.format(UPDATE_URL_LIST.get(i), brand);
                            URL url = new URL(_url);
                            log("URL: ", url);

                            stream = setupConnection(url, attempt);
                            if(url.toExternalForm().endsWith(".signed")) {
                                log("Request is signed, requiring valid signature");
                                stream = new SignedStream(stream);
                            }

                            UpdateMeta meta = fetchFrom(gson, Compressor.uncompressMarked(stream));

                            if(stream instanceof SignedStream) {
                                ((SignedStream) stream).validateSignature();
                            }

                            if(meta.isOutdated()) {
                                log("... is outdated, skipping");
                                continue;
                            }

                            updateProgress(1.);
                            log("Success!");

                            Bootstrap.recordBreadcrumb(UpdateMeta.class, "fetch_success",
                                    DataBuilder.create("url", url)
                                            .add("delta_ms", System.currentTimeMillis() - time)
                                            .add("attempt", attempt)
                            );

                            return meta;
                        } catch (Exception e) {
                            e.printStackTrace();
                            eList.add(e);

                            Bootstrap.recordBreadcrumbError(UpdateMeta.class, "fetch_failed", e, DataBuilder.create("url", _url)
                                    .add("delta_ms", System.currentTimeMillis() - time)
                                    .add("attempt", attempt)
                            );
                        } finally {
                            if(stream != null) {
                                U.close(stream);
                            }
                        }
                    }
                }

                throw new ExceptionList(eList);
            }
        };
    }

    private static Gson createGson() {
        return Json.build().registerTypeAdapter(UpdateMeta.class, new UpdateDeserializer()).create();
    }

    private static UpdateMeta fetchFrom(Gson gson, InputStream in) throws Exception {
        String read = null;
        try {
            read = IOUtils.toString(U.toReader(in));
            return gson.fromJson(read, UpdateMeta.class);
        } catch(Exception e) {
            Throwable cause = e;

            if(e.getCause() != null && e.getCause() instanceof IOException) {
                if(read == null) {
                    throw (IOException) e.getCause();
                }
                cause = e.getCause();
            }

            if(read == null) {
                throw e;
            }

            if(read.length() > 1000) {
                read = read.substring(0, 997) + "...";
            }

            throw new IOException("could not read: \""+ read +"\"", cause);
        }
    }

    public static UpdateMeta fetchFrom(InputStream in) throws Exception {
        return fetchFrom(createGson(), in);
    }

    private static InputStream setupConnection(URL url, int attempt) throws IOException {
        int timeout = attempt * INITIAL_TIMEOUT;

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);

        return connection.getInputStream();
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static Calendar calendar() {
        return Calendar.getInstance(UTC);
    }

    private static Calendar calendar(long millis) {
        Calendar c = calendar();
        c.setTimeInMillis(millis);
        return c;
    }

    protected long pendingUpdateUTC;
    protected UpdateEntry update;
    protected Map<String, String> support;
    protected Map<String, String> description; // will be added to RemoteLauncherMeta by UpdateDeserializer
    @Expose
    protected String options; // this field is handled by UpdateDeserializer

    public boolean isOutdated() {
        if(pendingUpdateUTC < 0) {
            return false;
        }
        if(pendingUpdateUTC == 0) {
            return true;
        }
        return calendar().after(calendar(pendingUpdateUTC * 1000));
    }

    public RemoteBootstrapMeta getBootstrap() {
        return update == null? null : update.bootstrap;
    }

    public RemoteLauncherMeta getLauncher() {
        return update == null? null : update.launcher;
    }

    public Map<String, String> getSupport() {
        return support == null ? null : Collections.unmodifiableMap(support);
    }

    public Map<String, String> getDescription() {
        return description == null? null : Collections.unmodifiableMap(description);
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    private static void log(Object... o) {
        U.log("[UpdateMeta]", o);
    }

    private static class UpdateEntry {
        private RemoteBootstrapMeta bootstrap;
        private RemoteLauncherMeta launcher;
    }
}
