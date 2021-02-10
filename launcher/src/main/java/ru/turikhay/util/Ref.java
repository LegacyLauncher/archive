package ru.turikhay.util;

public class Ref<V> {
    private volatile V value;

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
