package ru.turikhay.util;

import java.util.Objects;

public class E<K, V> {
    private final K key;
    private final V value;

    public E(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        E<?, ?> e = (E<?, ?>) o;
        return Objects.equals(key, e.key) && Objects.equals(value, e.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
