package ru.turikhay.tlauncher.bootstrap.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WeakObjectPool<T> {
    private final Factory<T> factory;
    private final List<ObjectRef<T>> objects = new ArrayList<ObjectRef<T>>();

    public WeakObjectPool(Factory<T> factory) {
        this.factory = U.requireNotNull(factory, "factory");
    }

    public synchronized ObjectRef<T> get() {
        Iterator<ObjectRef<T>> i = objects.iterator();
        while (i.hasNext()) {
            ObjectRef<T> ref = i.next();

            if(ref.trySetUsable()) {
                return ref;
            }

            if(ref.ref.get() == null) {
                i.remove();
            }
        }

        ObjectRef<T> newRef = new ObjectRef<T>(U.requireNotNull(factory.createNew(), "newObj"));
        objects.add(newRef);

        return newRef;
    }

    public final class ObjectRef<T> {
        private final WeakReference<T> ref;
        private T strongRef;
        private final AtomicBoolean inUse = new AtomicBoolean(true); // ObjectRefs are created on demand, it means that they must be set "in use"

        private ObjectRef(T obj) {
            this.ref = new WeakReference<T>(U.requireNotNull(obj, "object"));
        }

        public T get() {
            if(inUse.get()) {
                return ref.get();
            }
            throw new IllegalStateException("object "+ ref +" is not intended to be used");
        }

        public void free() {
            if(inUse.compareAndSet(true, false)) {
                strongRef = null;
            } else {
                throw new IllegalStateException("object "+ ref +" was not in use");
            }
        }

        private boolean trySetUsable() {
            if(inUse.compareAndSet(false, true)) {
                strongRef = ref.get();
                return strongRef != null;
            }
            return false;
        }

        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(super.equals(o)) {
                return true;
            }
            if(!(o instanceof ObjectRef)) {
                return false;
            }

            final Object obj = get(), compare = ((ObjectRef) o).get();

            if(obj == null) {
                return compare == null;
            }
            return obj.equals(compare);
        }

        public String toString() {
            final T obj = ref.get();
            final boolean use = inUse.get();
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("obj", obj).append("inUse", use).build();
        }
    }
}
