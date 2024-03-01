package net.legacylauncher.util.shared;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

import static net.legacylauncher.util.shared.FutureUtils.*;

class FutureUtilsTest {
    @RepeatedTest(10)
    void testInterruptibility() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        try {
            CompletableFuture<String> f = supplyInterruptible(() -> {
                started.countDown();
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e) {
                    interrupted.countDown();
                }
                return "I am supposed to fail";
            }, executor);
            started.await();
            f.cancel(true);
            assertTrue(interrupted.await(200, TimeUnit.MILLISECONDS), "interruption timed out");
            assertThrows(CancellationException.class, f::get);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testCancellation() {
        CompletableFuture<?> child = new CompletableFuture<>();
        CompletableFuture<?> combined = fastestOf(Collections.singleton(child));
        combined.cancel(true);
        assertTrue(child.isCancelled());
    }
}