package ru.turikhay.tlauncher.bootstrap.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class WeakObjectPool<T> {
    private final Supplier<T> factory;
    private final List<ObjectRef<T>> objects = new ArrayList<>();

    public WeakObjectPool(Supplier<T> factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public synchronized ObjectRef<T> get() {
        Iterator<ObjectRef<T>> i = objects.iterator();
        while (i.hasNext()) {
            ObjectRef<T> ref = i.next();

            if (ref.trySetUsable()) {
                return ref;
            }

            if (ref.ref.get() == null) {
                i.remove();
            }
        }

        ObjectRef<T> newRef = new ObjectRef<>(Objects.requireNonNull(factory.get(), "newObj"));
        objects.add(newRef);

        return newRef;
    }

    public static final class ObjectRef<T> {
        private final WeakReference<T> ref;
        private T strongRef;
        private final AtomicBoolean inUse = new AtomicBoolean(true); // ObjectRefs are created on demand, it means that they must be set "in use"

        private ObjectRef(T obj) {
            this.ref = new WeakReference<>(Objects.requireNonNull(obj, "object"));
        }

        public T get() {
            if (inUse.get()) {
                return ref.get();
            }
            throw new IllegalStateException("object " + ref + " is not intended to be used");
        }

        public void free() {
            if (inUse.compareAndSet(true, false)) {
                strongRef = null;
            } else {
                throw new IllegalStateException("object " + ref + " was not in use");
            }
        }

        private boolean trySetUsable() {
            if (inUse.compareAndSet(false, true)) {
                strongRef = ref.get();
                return strongRef != null;
            }
            return false;
        }

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (super.equals(o)) {
                return true;
            }
            if (!(o instanceof ObjectRef)) {
                return false;
            }

            final Object obj = get(), compare = ((ObjectRef<?>) o).get();

            return Objects.equals(obj, compare);
        }

        public String toString() {
            final T obj = ref.get();
            final boolean use = inUse.get();
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("obj", obj).append("inUse", use).build();
        }
    }
}
