package net.legacylauncher.bootstrap.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ThreadFactoryUtils {

    public static DIThreadFactory prefixedFactory(String prefix) {
        return new DIThreadFactory(new PrefixedNameFactory(prefix));
    }

    private ThreadFactoryUtils() {
    }

    public static class DIThreadFactory implements ThreadFactory {
        private final Function<Runnable, Thread> factory;
        private final Consumer<Thread> modifier;

        private DIThreadFactory(Function<Runnable, Thread> factory, Consumer<Thread> modifier) {
            this.factory = factory;
            this.modifier = modifier;
        }

        private DIThreadFactory(Function<Runnable, Thread> factory) {
            this(factory, t -> {});
        }

        private DIThreadFactory modify(Consumer<Thread> newModifier) {
            return new DIThreadFactory(factory, modifier.andThen(newModifier));
        }

        public DIThreadFactory makeDaemons() {
            return modify(DAEMONIZER);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = factory.apply(r);
            modifier.accept(thread);
            return thread;
        }
    }

    private static class PrefixedNameFactory implements Function<Runnable, Thread> {
        private final AtomicInteger counter = new AtomicInteger();
        private final String prefix;

        public PrefixedNameFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread apply(Runnable runnable) {
            return new Thread(runnable, prefix + "-" + counter.getAndIncrement());
        }
    }

    private static final Consumer<Thread> DAEMONIZER = t -> t.setDaemon(true);
}
