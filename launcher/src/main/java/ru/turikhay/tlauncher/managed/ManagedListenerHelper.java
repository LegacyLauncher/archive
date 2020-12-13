package ru.turikhay.tlauncher.managed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.U;

import java.util.ArrayList;
import java.util.List;

public class ManagedListenerHelper<L extends ManagedListener> implements ManagedListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<ManagedListener> listeners = new ArrayList<>();

    public synchronized void addListener(ManagedListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void changedSet(ManagedSet set) {
        for(ManagedListener l : listeners) {
            try {
                l.changedSet(set);
            } catch(Exception e) {
                LOGGER.warn("Exception has been caught in of the listeners of {}: {}", set, l, e);
            }
        }
    }
}
