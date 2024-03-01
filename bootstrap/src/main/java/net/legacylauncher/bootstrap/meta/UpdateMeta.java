package net.legacylauncher.bootstrap.meta;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import net.legacylauncher.bootstrap.json.Json;
import net.legacylauncher.bootstrap.json.RemoteBootstrapDeserializer;
import net.legacylauncher.bootstrap.json.RemoteLauncherDeserializer;
import net.legacylauncher.bootstrap.json.UpdateDeserializer;
import net.legacylauncher.bootstrap.task.Task;
import net.legacylauncher.bootstrap.transport.SignedStream;
import net.legacylauncher.bootstrap.util.Compressor;
import net.legacylauncher.bootstrap.util.ThreadFactoryUtils;
import net.legacylauncher.repository.RepoPrefixV1;
import net.legacylauncher.util.shared.FutureUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;

public class UpdateMeta {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMeta.class);

    static {
        Compressor.init(); // init compressor
    }

    public static Task<UpdateMeta> fetchFor(final String shortBrand, ConnectionInterrupter interrupter) {
        Objects.requireNonNull(shortBrand, "brand");

        return new Task<UpdateMeta>("fetchUpdate") {
            @Override
            protected UpdateMeta execute() throws Exception {
                LOGGER.info("Requesting update for: " + shortBrand);

                Gson gson = createGson(shortBrand);
                List<String> prefixes = RepoPrefixV1.prefixesCdnFirst();

                ExecutorService executor = Executors.newFixedThreadPool(
                        Math.min(4, prefixes.size()),
                        ThreadFactoryUtils.prefixedFactory("UpdateMeta").makeDaemons()
                );
                CompletableFuture<UpdateMeta> result = FutureUtils.fastestOf(prefixes.stream()
                        .map(prefix ->
                                String.format(Locale.ROOT, "%s/brands/%s/bootstrap.json.mgz.signed",
                                        prefix, shortBrand)
                        )
                        .map(url -> FutureUtils.supplyInterruptible(() -> {
                            LOGGER.debug("Fetching from: {}", url);
                            try (InputStream _in = new URL(url).openStream()) {
                                InputStream in = _in;
                                if (url.endsWith(".mgz.signed")) {
                                    in = new SignedStream(in);
                                }
                                UpdateMeta updateMeta = fetchFrom(gson, Compressor.uncompressMarked(in));
                                LOGGER.info("Successfully fetched from {}", url);
                                return updateMeta;
                            } catch (Exception e) {
                                LOGGER.warn("Couldn't fetch from {}", url, e);
                                throw e;
                            }
                        }, executor))
                );
                ScheduledThreadPoolExecutor delayer = null;
                if (interrupter != null) {
                    delayer = new ScheduledThreadPoolExecutor(
                            1,
                            ThreadFactoryUtils.prefixedFactory("UpdateMetaScheduler").makeDaemons()
                    );
                    delayer.setRemoveOnCancelPolicy(true);
                    delayer.schedule(
                            () -> interrupter.mayInterruptConnection(() -> result.cancel(true)),
                            5,
                            TimeUnit.SECONDS
                    );
                }
                try {
                    return result.get();
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
                    executor.shutdown();
                    if (delayer != null) {
                        delayer.shutdown();
                    }
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
