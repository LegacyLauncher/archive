package ru.turikhay.tlauncher.managed;

public interface ManagedListener<T> {
    void changedSet(ManagedSet<T> set);
}
