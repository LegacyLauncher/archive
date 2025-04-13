package net.legacylauncher.bootstrap.meta;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.json.Json;
import net.legacylauncher.bootstrap.json.RemoteBootstrapDeserializer;
import net.legacylauncher.bootstrap.json.RemoteLauncherDeserializer;
import net.legacylauncher.bootstrap.json.UpdateDeserializer;
import net.legacylauncher.bootstrap.task.Task;
import net.legacylauncher.bootstrap.transport.SignedStream;
import net.legacylauncher.bootstrap.util.BootstrapUserAgent;
import net.legacylauncher.bootstrap.util.Compressor;
import net.legacylauncher.repository.RepoPrefixV1;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@Slf4j
public class UpdateMeta {
    static {
        Compressor.init(); // init compressor
    }

    private static final int INITIAL_TIMEOUT = 1000, MAX_ATTEMPTS = 3, UPDATE_META_THREADS = 2;

    public static Task<UpdateMeta> fetchFor(final String shortBrand, ConnectionInterrupter interrupter) {
        Objects.requireNonNull(shortBrand, "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                ExecutorService executor = Executors.newFixedThreadPool(UPDATE_META_THREADS, new ThreadFactory() {
                    private final AtomicInteger i = new AtomicInteger();
                    @Override

                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(null, r, "UpdateMeta-" + i.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                });

                final AtomicBoolean canCancel = new AtomicBoolean(true);
                setupInterruption(canCancel, interrupter);

                try {
                    return fetchUpdateMeta(executor);
                } catch (InterruptedException e) {
                    log.warn("Update meta request was cancelled");
                    throw new UpdateMetaFetchFailed();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    log.error("Update meta request failed", cause);
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    }
                    throw e;
                } finally {
                    canCancel.set(false);
                    executor.shutdownNow();
                }
            }

            private UpdateMeta fetchUpdateMeta(ExecutorService executor) throws Exception {
                log.info("Requesting update for: {}", shortBrand);

                Gson gson = createGson(shortBrand);
                List<String> urlPrefixes = RepoPrefixV1.prefixesCdnFirst();
                String updatePath = "/brands/" + shortBrand + "/bootstrap.json.mgz.signed";

                for(int i = 1; i <= MAX_ATTEMPTS; i++) {
                    final int attempt = i;

                    CompletableFuture<UpdateMeta> completed = new CompletableFuture<>();
                    CompletableFuture.allOf(urlPrefixes.stream().map(urlPrefix -> {
                        final URL url = toUrl(urlPrefix + updatePath);
                        return CompletableFuture.runAsync(
                                () -> {
                                    UpdateMeta updateMeta = fetchAttempts(url, gson, attempt, completed::isDone);
                                    if (updateMeta == null) {
                                        return;
                                    }
                                    if (completed.complete(updateMeta)) {
                                        log.info("{} ({} / {}): Using response from this repo", url.getHost(), attempt, MAX_ATTEMPTS);
                                    } else {
                                        log.debug("{} ({} / {}): Successful, but ignored", url.getHost(), attempt, MAX_ATTEMPTS);
                                    }
                                },
                                executor
                        );
                    }).toArray(CompletableFuture[]::new)).whenComplete((v, t) ->
                            completed.completeExceptionally(new UpdateMetaFetchFailed())
                    );
                    try {
                        return completed.get(); // throws InterruptedException
                    } catch (CompletionException e) {
                        log.warn("Attempt {}: All update meta request were failed", attempt);
                    }
                }

                throw new UpdateMetaFetchFailed();
            }

            private void setupInterruption(AtomicBoolean canCancel, ConnectionInterrupter interrupter) {
                final Thread thisThread = Thread.currentThread();
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(() -> {
                    interrupter.mayInterruptConnection(() -> {
                        if (canCancel.get()) {
                            thisThread.interrupt();
                        }
                    });
                    executor.shutdown();
                }, 10, TimeUnit.SECONDS);
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

    private static UpdateMeta fetchAttempts(URL url, Gson gson, int attempt, BooleanSupplier finished) throws CompletionException {
        if (finished.getAsBoolean()) {
            log.debug("{} ({} / {}): Skipping", url.getHost(), attempt, MAX_ATTEMPTS);
            return null;
        }
        log.info("{} ({} / {}): Requesting {}", url.getHost(), attempt, MAX_ATTEMPTS, url);
        InputStream stream = null;
        try {
            long start = System.currentTimeMillis();
            stream = setupConnection(url, attempt);
            if (url.getPath().endsWith(".signed")) {
                log.debug("{} ({} / {}): Requiring valid signature", url.getHost(), attempt, MAX_ATTEMPTS);
                stream = new SignedStream(stream);
            }
            UpdateMeta meta = fetchFrom(gson, Compressor.uncompressMarked(stream));
            if (stream instanceof SignedStream) {
                try {
                    ((SignedStream) stream).validateSignature();
                } catch (IOException e) {
                    log.error("{} ({} / {}): Bad signature", url.getHost(), attempt, MAX_ATTEMPTS, e);
                    throw e;
                }
            }
            if (Thread.interrupted()) {
                log.info("{} ({} / {}): Interrupted", url.getHost(), attempt, MAX_ATTEMPTS);
                throw new CompletionException(new InterruptedException());
            }
            log.info("{} ({} / {}): OK ({} ms)", url.getHost(), attempt, MAX_ATTEMPTS,
                    (System.currentTimeMillis() - start));
            return meta;
        } catch (UnknownHostException e) {
            log.warn("{} ({} / {}): Unknown host", url.getHost(), attempt, MAX_ATTEMPTS);
        } catch (IOException e) {
            log.warn("{} ({} / {}): Couldn't fetch", url.getHost(), attempt, MAX_ATTEMPTS, e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        throw new CompletionException(new IOException("update meta is not available at " + url));
    }

    private static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Very bad url: " + url, e);
        }
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
        connection.setReadTimeout(5 * timeout);
        BootstrapUserAgent.set(connection);

        return connection.getInputStream();
    }

    @Getter
    protected RemoteBootstrapMeta bootstrap;
    protected RemoteLauncherMeta launcher, launcherBeta;
    @Getter @Expose
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

    public RemoteLauncherMeta getLauncher(boolean beta) {
        return beta && launcherBeta != null ? launcherBeta : launcher;
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
