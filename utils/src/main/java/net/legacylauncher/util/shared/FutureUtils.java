package net.legacylauncher.util.shared;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FutureUtils {
    public static <V> CompletableFuture<V> fastestOf(Collection<CompletableFuture<V>> futures) {
        // NB: none of futures are expected to return null as a valid result
        CompletableFuture<V> combined = new CompletableFuture<>();
        AtomicInteger countdown = new AtomicInteger(futures.size());
        futures.forEach(f -> f.whenComplete((result, ex) -> {
            boolean last = countdown.decrementAndGet() == 0;
            if (result != null) {
                combined.complete(result);
                if (!last) {
                    cancelAll(futures);
                }
            } else if (last) {
                combined.completeExceptionally(new NoTasksHaveCompletedException());
            }
        }));
        propagateCancellation(combined, futures);
        return combined;
    }

    public static <V> CompletableFuture<V> fastestOf(Stream<CompletableFuture<V>> futures) {
        return fastestOf(futures.collect(Collectors.toList()));
    }

    public static <V> CompletableFuture<V> supplyInterruptible(Callable<V> c, Executor executor) {
        AtomicReference<Thread> threadRef = new AtomicReference<>();
        CompletableFuture<V> f = new CompletableFuture<>();
        executor.execute(() -> {
            if (f.isDone()) {
                return;
            }
            threadRef.set(Thread.currentThread());
            V result;
            try {
                result = c.call();
            } catch (Throwable t) {
                f.completeExceptionally(t);
                return;
            } finally {
                threadRef.set(null);
            }
            f.complete(result);
        });
        f.whenComplete((__, t) -> {
            if (t instanceof CancellationException) {
                Thread thread = threadRef.get();
                if (thread != null && !thread.equals(Thread.currentThread())) {
                    thread.interrupt();
                }
            }
        });
        return f;
    }

    public static void propagateCancellation(CompletableFuture<?> parent, Collection<? extends Future<?>> children) {
        parent.whenComplete((__, t) -> {
            if (t instanceof CancellationException) {
                cancelAll(children);
            }
        });
    }

    static void cancelAll(Collection<? extends Future<?>> futures) {
        futures.forEach(f -> f.cancel(true));
    }

    public static class NoTasksHaveCompletedException extends RuntimeException {
        public NoTasksHaveCompletedException() {
            super("All tasks have failed", null, false, false);
        }
    }
}
