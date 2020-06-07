package ru.turikhay.tlauncher.bootstrap.util;

public final class Ref<T> {
    private T object;

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
