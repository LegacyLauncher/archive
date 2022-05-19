package ru.turikhay.tlauncher.bootstrap.meta;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.json.RemoteBootstrapDeserializer;
import ru.turikhay.tlauncher.bootstrap.json.RemoteLauncherDeserializer;
import ru.turikhay.tlauncher.bootstrap.json.UpdateDeserializer;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.transport.SignedStream;
import ru.turikhay.tlauncher.bootstrap.util.Compressor;
import ru.turikhay.tlauncher.repository.RepoPrefixV1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UpdateMeta {
    private static final List<String> UPDATE_URL_LIST;

    static {
        final String updatePathFormat = "/brands/%s/bootstrap.json.mgz.signed";
        List<String> updateUrlList = RepoPrefixV1.prefixesCdnFirst().stream().map(prefix ->
                prefix + updatePathFormat
        ).collect(
                Collectors.toList()
        );
        UPDATE_URL_LIST = Collections.unmodifiableList(updateUrlList);

        Compressor.init(); // init compressor
    }

    private static final int INITIAL_TIMEOUT = 2000, MAX_ATTEMPTS = 5;

    public static Task<UpdateMeta> fetchFor(final String shortBrand, ConnectionInterrupter interrupter) {
        Objects.requireNonNull(shortBrand, "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                log("Requesting update for: " + shortBrand);

                Gson gson = createGson(shortBrand);
                AtomicBoolean updateRequestCancelled = new AtomicBoolean();
                Exception error = new UpdateMetaFetchFailed();

                for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                    if (attempt == 2 && interrupter != null) {
                        interrupter.mayInterruptConnection(() -> updateRequestCancelled.set(true));
                    }
                    for (int i = 0; i < UPDATE_URL_LIST.size(); i++) {
                        if (updateRequestCancelled.get()) {
                            throw error;
                        }
                        checkInterrupted();
                        updateProgress((i + 1f) / UPDATE_URL_LIST.size());

                        String _url;
                        long time = System.currentTimeMillis();

                        InputStream stream = null;
                        try {
                            _url = String.format(Locale.ROOT, UPDATE_URL_LIST.get(i), shortBrand);
                            URL url = new URL(_url);
                            log("URL: ", url);

                            stream = setupConnection(url, attempt);
                            if (url.toExternalForm().endsWith(".signed")) {
                                log("Request is signed, requiring valid signature");
                                stream = new SignedStream(stream);
                            }

                            UpdateMeta meta = fetchFrom(gson, Compressor.uncompressMarked(stream));

                            if (stream instanceof SignedStream) {
                                ((SignedStream) stream).validateSignature();
                            }

                            if (meta.isOutdated()) {
                                log("... is outdated");
                                log("Current time:", time);
                                log("Time in meta:", meta.getPendingUpdateUTC() * 1000L);
                                error.addSuppressed(new OutdatedUpdateMetaException(
                                        _url,
                                        format(calendar(time)),
                                        format(calendar(meta.getPendingUpdateUTC() * 1000L))
                                ));
                                continue;
                            }

                            updateProgress(1.);
                            log("Success!");
                            return meta;
                        } catch (Exception e) {
                            e.printStackTrace();
                            error.addSuppressed(e);
                        } finally {
                            if (stream != null) {
                                stream.close();
                            }
                        }
                    }
                }

                throw error;
            }
        };
    }

    private static Gson createGson(String shortBrand) {
        return Json.build()
                .registerTypeAdapter(UpdateMeta.class, new UpdateDeserializer())
                .registerTypeAdapter(RemoteLauncherMeta.class, new RemoteLauncherDeserializer(shortBrand))
                .registerTypeAdapter(RemoteBootstrapMeta.class, new RemoteBootstrapDeserializer(shortBrand))
                .create();
    }

    private static UpdateMeta fetchFrom(Gson gson, InputStream in) throws Exception {
        String read = null;
        try {
            read = IOUtils.toString(in, StandardCharsets.UTF_8);
            return gson.fromJson(read, UpdateMeta.class);
        } catch (Exception e) {
            Throwable cause = e;

            if (e.getCause() != null && e.getCause() instanceof IOException) {
                if (read == null) {
                    throw (IOException) e.getCause();
                }
                cause = e.getCause();
            }

            if (read == null) {
                throw e;
            }

            if (read.length() > 1000) {
                read = read.substring(0, 997) + "...";
            }

            throw new IOException("could not read: \"" + read + "\"", cause);
        }
    }

    public static UpdateMeta fetchFrom(InputStream in, String shortBrand) throws Exception {
        return fetchFrom(createGson(shortBrand), in);
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

    private static SimpleDateFormat FORMAT;

    private static String format(Calendar calendar) {
        if (FORMAT == null) {
            FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
        }
        return FORMAT.format(calendar.getTime());
    }

    protected long pendingUpdateUTC;
    protected RemoteBootstrapMeta bootstrap;
    protected RemoteLauncherMeta launcher, launcherBeta;
    @Expose
    protected String options; // this field is handled by UpdateDeserializer

    public UpdateMeta(long pendingUpdateUTC,
                      RemoteBootstrapMeta bootstrap,
                      RemoteLauncherMeta launcher,
                      RemoteLauncherMeta launcherBeta,
                      String options) {
        this.pendingUpdateUTC = pendingUpdateUTC;
        this.bootstrap = bootstrap;
        this.launcher = Objects.requireNonNull(launcher, "launcher");
        this.launcherBeta = launcherBeta;
        this.options = options;
    }

    public boolean isOutdated() {
        if (pendingUpdateUTC < 0) {
            return false;
        }
        if (pendingUpdateUTC == 0) {
            return true;
        }
        return calendar().after(calendar(pendingUpdateUTC * 1000));
    }

    public long getPendingUpdateUTC() {
        return pendingUpdateUTC;
    }

    public RemoteBootstrapMeta getBootstrap() {
        return bootstrap;
    }

    public RemoteLauncherMeta getLauncher(boolean beta) {
        return beta && launcherBeta != null ? launcherBeta : launcher;
    }

    public String getOptions() {
        return options;
    }

    public interface ConnectionInterrupter {
        void mayInterruptConnection(Callback callback);

        interface Callback {
            void onConnectionInterrupted();
        }
    }

    public static class UpdateMetaFetchFailed extends Exception {
        UpdateMetaFetchFailed() {
            super("Couldn't fetch UpdateMeta");
        }
    }
}
