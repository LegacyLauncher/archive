package ru.turikhay.tlauncher.managed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ManagedListenerHelper<T> implements ManagedListener<T> {
    private static final Logger LOGGER = LogManager.getLogger();

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
                LOGGER.warn("Exception has been caught in of the listeners of {}: {}", set, l, e);
            }
        }
    }
}
