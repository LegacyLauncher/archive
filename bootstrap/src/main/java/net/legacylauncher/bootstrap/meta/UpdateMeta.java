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
import net.legacylauncher.bootstrap.util.U;
import net.legacylauncher.repository.HostsV1;
import net.legacylauncher.connection.ConnectionQueue;
import net.legacylauncher.connection.ConnectionSelector;
import net.legacylauncher.connection.bad.BadHostsFilter;
import net.legacylauncher.connection.HttpConnection;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class UpdateMeta {
    static {
        Compressor.init(); // init compressor
    }

    private static final int
            CONNECT_TIMEOUT = 15_000,
            READ_TIMEOUT = 3_000,
            NEW_REQUEST_TIMEOUT = 2_000,
            MAX_ATTEMPTS = 3,
            UPDATE_META_THREADS = 4;

    public static Task<UpdateMeta> fetchFor(final String shortBrand, ConnectionInterrupter interrupter) {
        Objects.requireNonNull(shortBrand, "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                final AtomicBoolean canCancel = new AtomicBoolean(true);
                setupInterruption(canCancel, interrupter);
                ScheduledExecutorService scheduler = ConnectionSelector.createScheduler(UPDATE_META_THREADS);
                try {
                    return fetchUpdateMeta(scheduler);
                } catch (InterruptedException e) {
                    log.warn("Update meta request was cancelled");
                    throw new UpdateMetaFetchFailed();
                } finally {
                    canCancel.set(false);
                    scheduler.shutdown();
                }
            }

            private UpdateMeta fetchUpdateMeta(ScheduledExecutorService scheduler) throws InterruptedException, UpdateMetaFetchFailed {
                log.info("Requesting update for: {}", shortBrand);

                Gson gson = createGson(shortBrand);
                List<URL> urlList = HostsV1.BOOTSTRAP.stream().map(host -> String.format(Locale.ROOT,
                        "https://%s/%s/bootstrap.json.mgz.signed",
                        host, shortBrand
                ))
                        .map(U::toUrl)
                        .collect(Collectors.toList());

                for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                    long start = System.currentTimeMillis();
                    CompletableFuture<ConnectionQueue<HttpConnection>> future = ConnectionSelector.create(
                            new BadHostsFilter<>(U.BAD_HOSTS, new HttpConnection.Connector(
                                    CONNECT_TIMEOUT * attempt,
                                    READ_TIMEOUT * attempt,
                                    BootstrapUserAgent.USER_AGENT,
                                    U.getProxy()
                            )),
                            NEW_REQUEST_TIMEOUT * attempt,
                            TimeUnit.MILLISECONDS,
                            scheduler
                    ).select(urlList);
                    ConnectionQueue<HttpConnection> queue;
                    try {
                        queue = future.get();
                    } catch (ExecutionException e) {
                        log.warn("({} / {}): Couldn't connect to any UpdateMeta repo", attempt, MAX_ATTEMPTS, e.getCause());
                        continue;
                    } catch (InterruptedException e) {
                        log.info("({} / {}): Interrupted", attempt, MAX_ATTEMPTS);
                        future.completeExceptionally(e); // cancel subsequent request, if any
                        throw e;
                    }
                    try {
                        return readQueue(queue, attempt, gson, start);
                    } catch (IOException e) {
                        // continue
                    } finally {
                        queue.close();
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

    private static UpdateMeta readQueue(
            ConnectionQueue<HttpConnection> queue,
            int attempt,
            Gson gson,
            long start
    ) throws InterruptedException, IOException {
        while (true) {
            HttpConnection c = queue.takeOrThrow(() -> new IOException("No available connection"));
            HttpURLConnection connection = c.getConnection();
            URL url = connection.getURL();
            log.info("({} / {}): Reading {}", attempt, MAX_ATTEMPTS, url);
            InputStream stream = null;
            try {
                stream = connection.getInputStream();
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
                        U.BAD_HOSTS.add(url); // very bad, do not like
                        throw e;
                    }
                }
                if (Thread.interrupted()) {
                    log.info("{} ({} / {}): Interrupted", url.getHost(), attempt, MAX_ATTEMPTS);
                    throw new InterruptedException();
                }
                log.info("{} ({} / {}): OK ({} ms)", url.getHost(), attempt, MAX_ATTEMPTS,
                        (System.currentTimeMillis() - start));
                return meta;
            } catch (IOException e) {
                log.warn("{} ({} / {}): Couldn't fetch", url.getHost(), attempt, MAX_ATTEMPTS, e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private static Gson createGson(String shortBrand) {
        return Json.build()
                .registerTypeAdapter(UpdateMeta.class, new UpdateDeserializer())
                .registerTypeAdapter(RemoteLauncherMeta.class, new RemoteLauncherDeserializer(shortBrand))
                .registerTypeAdapter(RemoteBootstrapMeta.class, new RemoteBootstrapDeserializer(shortBrand))
                .create();
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

    @Getter
    protected RemoteBootstrapMeta bootstrap;
    protected RemoteLauncherMeta launcher, launcherBeta;
    @Getter
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

    public RemoteLauncherMeta getLauncher(boolean beta) {
        if (beta) {
            if (launcherBeta == null) {
                log.warn("Not launcherBeta entry inside UpdateMeta");
            } else {
                return launcherBeta;
            }
        }
        return launcher;
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
