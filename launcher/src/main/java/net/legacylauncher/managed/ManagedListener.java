package net.legacylauncher.managed;

public interface ManagedListener<T> {
    void changedSet(ManagedSet<T> set);
}
