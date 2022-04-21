package ru.turikhay.util.async;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsyncObjectContainer<T> {
    private final List<AsyncObject<T>> objects = new ArrayList<>();
    private final Map<AsyncObject<T>, T> values = new LinkedHashMap<>();

    private final Object waitLock = new Object();
    private boolean executionLock;

    public Map<AsyncObject<T>, T> execute() {
        executionLock = true;
        values.clear();

        synchronized (objects) {
            for (AsyncObject<T> object : objects)
                object.start();

            boolean hasRemaining;

            do {
                hasRemaining = false;

                for (AsyncObject<T> object : objects) {
                    try {
                        if (values.containsKey(object)) {
                            continue;
                        }

                        values.put(object, object.getValue());
                    } catch (AsyncObjectNotReadyException ignored) {
                        hasRemaining = true;
                    } catch (AsyncObjectGotErrorException ignored0) {
                        values.put(object, null);
                    }
                }

                if (hasRemaining) {
                    synchronized (waitLock) {
                        try {
                            waitLock.wait();
                        } catch (InterruptedException e) {
                            return null;
                        }
                    }
                }
            } while (hasRemaining);
        }

        executionLock = false;
        return values;
    }

    public void add(AsyncObject<T> object) {
        if (object == null) {
            throw new NullPointerException();
        }

        if (object.getContainer() != null) {
            throw new IllegalArgumentException();
        }

        synchronized (objects) {
            if (executionLock)
                throw new AsyncContainerLockedException();

            objects.add(object);
            object.setContainer(this);
        }
    }

    public void remove(AsyncObject<T> object) {
        if (object == null)
            throw new NullPointerException();

        if (object.getContainer() != this) {
            return;
        }

        synchronized (objects) {
            if (executionLock)
                throw new AsyncContainerLockedException();

            objects.remove(object);
        }
    }

    public void removeAll() {
        synchronized (objects) {
            if (executionLock)
                throw new AsyncContainerLockedException();

            objects.clear();
        }
    }

    void release() {
        synchronized (waitLock) {
            waitLock.notifyAll();
        }
    }
}
