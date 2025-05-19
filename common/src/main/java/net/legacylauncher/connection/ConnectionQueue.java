package net.legacylauncher.connection;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectionQueue<C extends Connection> implements AutoCloseable, Closeable {
    ArrayBlockingQueue<C> queue;
    AtomicBoolean closed = new AtomicBoolean(false);

    ConnectionQueue(int size) {
        this.queue = new ArrayBlockingQueue<>(size);
    }

    boolean offer(C connection) {
        if (isClosed()) {
            return false;
        }
        log.trace("New connection available: {}", connection);
        return queue.offer(connection);
    }

    public <X extends Throwable> C takeOrThrow(Supplier<X> error) throws InterruptedException, X {
        if (isClosed()) {
            throw error.get();
        }
        return queue.take();
    }

    public C takeOrThrow() throws InterruptedException, IOException {
        return takeOrThrow(() -> new IOException("No connections available"));
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            queue.removeIf(c -> {
                c.disconnect();
                return true;
            });
        }
    }

    public boolean isClosed() {
        return closed.get();
    }
}
