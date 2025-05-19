package net.legacylauncher.connection;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.connection.bad.BadHostException;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectionSelector<C extends Connection> {
    static final TimeUnit UNIT = TimeUnit.MILLISECONDS;
    static final Random RAND = new Random();

    public static <C extends Connection> ConnectionSelector<C> create(
            UrlConnector<C> connector,
            long requestsDelay,
            TimeUnit unit,
            ScheduledExecutorService delayer,
            Executor executor
    ) {
        return new ConnectionSelector<>(
                connector,
                unit.toMillis(requestsDelay),
                delayer,
                executor
        );
    }

    public static <C extends Connection> ConnectionSelector<C> create(
            UrlConnector<C> connector,
            long requestsDelay,
            TimeUnit unit,
            ScheduledExecutorService delayer
    ) {
        return create(connector, requestsDelay, unit, delayer, delayer);
    }

    public static <C extends Connection> ConnectionSelector<C> create(
            UrlConnector<C> connector,
            long requestsDelay,
            TimeUnit unit
    ) {
        return create(connector, requestsDelay, unit, createScheduler());
    }

    UrlConnector<C> connector;
    long delayBetweenAttempts;
    ScheduledExecutorService delayer;
    Executor executor;

    public CompletableFuture<ConnectionQueue<C>> select(Stream<URL> stream) {
        List<URL> list = stream.collect(Collectors.toList());
        Attempt attempt = new Attempt(list);
        attempt.start();
        return attempt.future;
    }

    public CompletableFuture<ConnectionQueue<C>> select(Collection<URL> list) {
        return select(list.stream());
    }

    private static int nextRand() {
        return 1000 + RAND.nextInt(9000);
    }

    private void delayAndExecute(Runnable r, long delay) {
        delayer.schedule(() -> executor.execute(r), delay, UNIT);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private class Attempt {
        List<URL> list;
        int key;
        ConcurrentLinkedQueue<URL> urlQueue;
        ConnectionQueue<C> connections;
        CompletableFuture<ConnectionQueue<C>> future = new CompletableFuture<>();
        ConcurrentLinkedQueue<UrlConnectionException> errors = new ConcurrentLinkedQueue<>();
        AtomicInteger ordinalCount = new AtomicInteger(0);

        private Attempt(List<URL> list) {
            this.list = list;
            this.key = nextRand();
            this.urlQueue = new ConcurrentLinkedQueue<>(list);;
            this.connections = new ConnectionQueue<>(list.size());
        }

        void start() {
            schedulePollAndPerform(0);
        }

        void schedulePollAndPerform(long delay) {
            log.trace("[{}] Scheduling next pool in {}", key, delay);
            try {
                delayAndExecute(this::pollAndPerform, delay);
            } catch (RejectedExecutionException e) {
                // this shouldn't happen under normal circumstances
                // but Selector might get stuck indefinitely if this goes unhandled
                log.error("[{}] Scheduler rejected the task", key);
                completeWithError(e);
            }
        }

        void pollAndPerform() {
            URL url = urlQueue.poll();
            if (url == null) {
                log.trace("[{}] No URLs in pool", key);
                // all other threads are probably busy
                return;
            }
            ConnectionInfo info = new ConnectionInfo(
                    url,
                    ordinalCount.incrementAndGet(),
                    list.size()
            );
            if (maySkip()) {
                clearQueue();
                log.debug("[{}:{}] Skipped before making the request", key, url.getHost());
                return;
            }
            // schedule next request in case this request will take its time
            schedulePollAndPerform(delayBetweenAttempts);
            try {
                performRequest(info);
            } catch (IOException ioE) {
                if (maySkip()) {
                    clearQueue();
                    // skip unnecessary error processing
                    return;
                }
                errors.offer(new UrlConnectionException(url, ioE));
                if (ioE instanceof BadHostException) {
                    log.debug("[{}:{}] Skipping bad host", key, url.getHost());
                } else {
                    log.warn("[{}:{}] Request failed", key, url.getHost(), ioE);
                }
                if (errors.size() == list.size()) {
                    log.error("[{}] All requests have failed", key);
                    completeWithError();
                } else {
                    // immediately make the next request, eliminating unnecessary waiting;
                    // request scheduled earlier may make it look like we don't respect the delay
                    schedulePollAndPerform(0);
                }
            } catch (Throwable t) {
                if (!maySkip()) {
                    // generally indicate serious problems like OOM
                    log.error("[{}:{}] An error occurred", key, url.getHost(), t);
                    completeWithError(t);
                }
                // double and give it to the next handler
                throw t;
            }
        }

        void performRequest(ConnectionInfo info) throws IOException {
            URL url = info.getUrl();
            log.debug("[{}:{}] Making request to {}", key, url.getHost(), url);
            Instant start = Instant.now();
            C connection = connector.connect(info);
            // connected successfully
            if (!connections.offer(connection)) {
                // connections queue was closed
                // => we are definitely not the first
                log.debug("[{}:{}:{}] Discarded", key, url.getHost(), ms(start));
                connection.disconnect();
                return;
            }
            // notify the user about available connection
            if (future.complete(connections)) {
                log.debug("[{}:{}:{}] OK", key, url.getHost(), ms(start));
            } else {
                // we are not the first
                log.debug("[{}:{}:{}] OK, queued", key, url.getHost(), ms(start));
            }
        }

        void completeWithError() {
            IOException ioE = new IOException("All requests with key " + key + " have failed");
            errors.forEach(ioE::addSuppressed);
            completeWithError(ioE);
        }

        void completeWithError(Throwable t) {
            clearQueue();
            connections.close();
            future.completeExceptionally(t);
        }

        boolean maySkip() {
            return connections.isClosed();
        }

        void clearQueue() {
//            log.debug("[{}] Clearing the queue", key);
            urlQueue.clear();
        }
    }

    private static final int DEFAULT_POOL_SIZE = 4;

    public static ScheduledExecutorService createScheduler(int poolSize) {
        int hash = RAND.nextInt();
        return Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {
            private final AtomicInteger i = new AtomicInteger();
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread t = new Thread(null, r, "ConnectionSelector-" + hash + "-" + i.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public static ScheduledExecutorService createScheduler() {
        return createScheduler(DEFAULT_POOL_SIZE);
    }

    private static String ms(Instant start) {
        return Duration.between(start, Instant.now()).toMillis() + "ms";
    }

}
