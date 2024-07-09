package net.legacylauncher.util.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AsyncObject<E> implements Callable<E> {
    private AsyncObjectContainer<E> container;

    private Future<E> future;

    protected AsyncObject() {
    }

    public final AsyncObjectContainer<E> getContainer() {
        return container;
    }

    void setContainer(AsyncObjectContainer<E> container) {
        this.container = container;
    }

    public void queue() {
        if (future != null) {
            throw new IllegalStateException();
        }
        future = AsyncThread.future(this);
    }

    @Override
    public E call() throws Exception {
        try {
            return execute();
        } finally {
            if (container != null) {
                container.release();
            }
        }
    }

    public E getValue() throws AsyncObjectNotReadyException, AsyncObjectGotErrorException {
        if (!future.isDone()) {
            throw new AsyncObjectNotReadyException();
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interruped", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new AsyncObjectGotErrorException(this, cause);
        }
    }

    protected abstract E execute() throws Exception;
}
