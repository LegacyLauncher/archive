package net.legacylauncher.managed;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ManagedListenerHelper<T> implements ManagedListener<T> {
    private final List<ManagedListener<T>> listeners = new ArrayList<>();

    public synchronized void addListener(ManagedListener<T> l) {
        listeners.add(l);
    }

    @Override
    public synchronized void changedSet(ManagedSet<T> set) {
        for (ManagedListener<T> l : listeners) {
            try {
                l.changedSet(set);
            } catch (Exception e) {
                log.warn("Exception has been caught in of the listeners of {}: {}", set, l, e);
            }
        }
    }
}
