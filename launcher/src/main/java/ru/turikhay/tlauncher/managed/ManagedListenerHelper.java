package ru.turikhay.tlauncher.managed;

import ru.turikhay.util.U;

import java.util.ArrayList;
import java.util.List;

public class ManagedListenerHelper<L extends ManagedListener> implements ManagedListener {
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
                U.log("ManagedListenerHelper caught exception ", set, l, e);
            }
        }
    }
}
