package ru.turikhay.tlauncher.bootstrap.meta;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateMeta {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMeta.class);

    static {
        Compressor.init(); // init compressor
    }

    private static final int INITIAL_TIMEOUT = 10000, MAX_ATTEMPTS = 3;

    public static Task<UpdateMeta> fetchFor(final String shortBrand, ConnectionInterrupter interrupter) {
        Objects.requireNonNull(shortBrand, "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                log("Requesting update for: " + shortBrand);

                Gson gson = createGson(shortBrand);
                List<String> urlPrefixes = RepoPrefixV1.prefixesCdnFirst();
                String updatePath = "/brands/" + shortBrand + "/bootstrap.json.mgz.signed";
                ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
                    private final AtomicInteger i = new AtomicInteger();
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(null, r, "UpdateMeta-" + i.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                });
                ScheduledThreadPoolExecutor delayer = new ScheduledThreadPoolExecutor(
                        1, r -> {
                            Thread t = new Thread(null, r, "UpdateMetaDelayScheduler");
                            t.setDaemon(true);
                            return t;
                        });
                delayer.setRemoveOnCancelPolicy(true);
                CompletableFuture<UpdateMeta> completed = new CompletableFuture<>();
                try {
                    List<CompletableFuture<UpdateMeta>> tasks = new ArrayList<>();
                    for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                        final int attempt = i;
                        for (int k = 0; k < urlPrefixes.size(); k++) {
                            CompletableFuture<Void> delay;
                            if (i == 1 && k < 2) {
                                delay = CompletableFuture.completedFuture(null);
                            } else {
                                delay = new CompletableFuture<>();
                                delayer.schedule(() -> delay.complete(null), 5, TimeUnit.SECONDS);
                            }
                            final String url = urlPrefixes.get(k) + updatePath;
                            tasks.add(delay.thenApplyAsync((__) -> fetchAttempts(url, gson, attempt), executor));
                        }
                    }
                    tasks.forEach(f -> f.thenAccept(completed::complete));
                    completed.whenComplete((__, t) -> {
                        tasks.forEach(c -> c.cancel(true));
                    });
                    CompletableFuture<?> all = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
                    all.whenComplete((__, t) -> {
                        if (t != null) {
                            completed.completeExceptionally(new UpdateMetaFetchFailed());
                        }
                    });
                    try {
                        return completed.get(5, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        LOGGER.warn("Fetching update meta is taking too long");
                        if (interrupter != null) {
                            interrupter.mayInterruptConnection(() -> completed.cancel(true));
                        }
                    }
                    return completed.get();
                } catch (CancellationException e) {
                    LOGGER.info("Update meta request was cancelled");
                    throw new UpdateMetaFetchFailed();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    LOGGER.error("Update meta request failed", cause);
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    }
                    throw e;
                } finally {
                    completed.cancel(true);
                    executor.shutdown();
                }
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

    private static UpdateMeta fetchAttempts(String sUrl, Gson gson, int attempt) throws CompletionException {
        URL url;
        try {
            url = new URL(sUrl);
        } catch (MalformedURLException e) {
            LOGGER.error("Bad url: {}", sUrl, e);
            throw new CompletionException("bad url: " + sUrl, e);
        }
        LOGGER.info("{} ({} / {}): Requesting {}", url.getHost(), attempt, MAX_ATTEMPTS, url);
        InputStream stream = null;
        try {
            long start = System.currentTimeMillis();
            stream = setupConnection(url, attempt);
            if (url.getPath().endsWith(".signed")) {
                LOGGER.debug("{} ({} / {}): Requiring valid signature", url.getHost(), attempt, MAX_ATTEMPTS);
                stream = new SignedStream(stream);
            }
            UpdateMeta meta = fetchFrom(gson, Compressor.uncompressMarked(stream));
            if (stream instanceof SignedStream) {
                try {
                    ((SignedStream) stream).validateSignature();
                } catch (IOException e) {
                    LOGGER.error("{} ({} / {}): Bad signature", url.getHost(), attempt, MAX_ATTEMPTS, e);
                    throw e;
                }
            }
            if (Thread.interrupted()) {
                LOGGER.info("{} ({} / {}): Interrupted", url.getHost(), attempt, MAX_ATTEMPTS);
                throw new CompletionException(new InterruptedException());
            }
            LOGGER.info("{} ({} / {}): OK ({} ms)", url.getHost(), attempt, MAX_ATTEMPTS,
                    (System.currentTimeMillis() - start));
            return meta;
        } catch (UnknownHostException e) {
            LOGGER.warn("{} ({} / {}): Unknown host", url.getHost(), attempt, MAX_ATTEMPTS);
        } catch (IOException e) {
            LOGGER.warn("{} ({} / {}): Couldn't fetch", url.getHost(), attempt, MAX_ATTEMPTS, e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        throw new CompletionException(new IOException("update meta is not available at " + url));
    }

    private static UpdateMeta fetchFrom(Gson gson, InputStream in) throws IOException {
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

    protected RemoteBootstrapMeta bootstrap;
    protected RemoteLauncherMeta launcher, launcherBeta;
    @Expose
    protected String options; // this field is handled by UpdateDeserializer

    public UpdateMeta(RemoteBootstrapMeta bootstrap,
                      RemoteLauncherMeta launcher,
                      RemoteLauncherMeta launcherBeta,
                      String options) {
        this.bootstrap = bootstrap;
        this.launcher = Objects.requireNonNull(launcher, "launcher");
        this.launcherBeta = launcherBeta;
        this.options = options;
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
