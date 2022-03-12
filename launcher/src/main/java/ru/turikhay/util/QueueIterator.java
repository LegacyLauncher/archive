package ru.turikhay.util;

import java.util.Iterator;

public class QueueIterator<V> implements Iterator<V> {
    private final Iterator<V> i;

    public QueueIterator(Iterator<V> i) {
        this.i = i;
    }

    @Override
    public boolean hasNext() {
        return i.hasNext();
    }

    @Override
    public V next() {
        V v = i.next();
        i.remove();
        return v;
    }
}
