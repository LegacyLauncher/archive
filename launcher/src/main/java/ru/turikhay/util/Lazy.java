package ru.turikhay.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class Lazy<T> implements Callable<T> {
    private static final Logger LOGGER = LogManager.getLogger(Lazy.class);

    private Callable<T> initializer; // gc collectable

    private boolean calledOnce;
    private Throwable exception;
    private T value;

    private Lazy(Callable<T> initializer) {
        this.initializer = initializer;
    }

    public boolean isInitialized() {
        return calledOnce;
    }

    public synchronized T get() throws LazyInitException {
        if (exception != null) {
            throw new LazyInitException(exception);
        }
        if (calledOnce) {
            return value;
        } else {
            calledOnce = true;
        }
        T value;
        try {
            value = initializer.call();
        } catch (Exception e) {
            LOGGER.debug("Initialization exception on {}", initializer, e);
            exception = e;
            throw new LazyInitException(e);
        } catch (Throwable e) {
            LOGGER.warn("Severe initialization error on {}", initializer, e);
            exception = e;
            throw new LazyInitException(e);
        } finally {
            initializer = null;
        }
        return this.value = value;
    }

    @Override
    public T call() throws Exception {
        try {
            return get();
        } catch (LazyInitException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new Error(cause); // should never happen
            }
        }
    }

    public Optional<T> value() {
        T value;
        try {
            value = get();
        } catch (LazyInitException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(value);
    }

    public Optional<T> valueIfInitialized() {
        if (isInitialized()) {
            return value();
        } else {
            return Optional.empty();
        }
    }

    public static <T> Lazy<T> of(Callable<T> callable) {
        return new Lazy<>(Objects.requireNonNull(callable));
    }

    @Override
    public String toString() {
        // we don't really care here about a race condition
        return "Lazy{" +
                (calledOnce ? "value=" + value : "initializer=" + initializer) +
                '}';
    }
}
