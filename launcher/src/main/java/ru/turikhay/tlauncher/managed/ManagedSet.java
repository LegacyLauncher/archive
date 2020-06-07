package ru.turikhay.tlauncher.managed;

import ru.turikhay.util.U;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ManagedSet<T> {
    private final Set<T> set = new LinkedHashSet<T>(), set_ = Collections.unmodifiableSet(set);
    private final ManagedListener listener;
    private T selected;

    protected ManagedSet(ManagedListener listener) {
        this.listener = U.requireNotNull(listener, "listener");
    }

    protected void fireSetChanged() {
        listener.changedSet(this);
    }

    public void add(T obj) {
        if(set.add(U.requireNotNull(obj, "user"))) {
            fireSetChanged();
        }
    }

    public void remove(T obj) {
        if(obj == null) {
            return;
        }
        if(set.remove(obj)) {
            if(obj.equals(selected)) {
                selected = null;
            }
            fireSetChanged();
        }
    }

    public void select(T obj, boolean fireRefresh) {
        if(!U.equal(selected, obj)) {
            selected = obj;
            if(fireRefresh) {
                fireSetChanged();
            }
        }
    }

    public void select(T obj) {
        select(obj, true);
    }

    public T getSelected() {
        return selected;
    }

    public Set<T> getSet() {
        return set_;
    }
}
