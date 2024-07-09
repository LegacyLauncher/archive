package net.legacylauncher.component;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.managers.ComponentManager;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

@Slf4j
public abstract class InterruptibleComponent extends RefreshableComponent {
    protected final boolean[] refreshList;
    private int lastRefreshID;
    protected final Semaphore semaphore;
    protected boolean lastResult;

    protected InterruptibleComponent(ComponentManager manager) {
        this(manager, 64);
    }

    private InterruptibleComponent(ComponentManager manager, int listSize) {
        super(manager);
        semaphore = new Semaphore(1);
        if (listSize < 1) {
            throw new IllegalArgumentException("Invalid list size: " + listSize + " < 1");
        } else {
            refreshList = new boolean[listSize];
        }
    }

    public final boolean refresh() {
        if (semaphore.tryAcquire()) {
            boolean result;
            try {
                result = lastResult = refresh(nextID());
            } finally {
                semaphore.release();
            }

            return result;
        } else {
            try {
                semaphore.acquire();
                return lastResult;
            } catch (InterruptedException e) {
                log.warn("Thread interrupted", e);
            } finally {
                semaphore.release();
            }

            return false;
        }
    }

    public final boolean isRefreshing() {
        return semaphore.hasQueuedThreads();
    }

    public synchronized void stopRefresh() {
        Arrays.fill(refreshList, false);
    }

    protected synchronized int nextID() {
        int listSize = refreshList.length;
        int next = lastRefreshID++;
        if (next >= listSize) {
            next = 0;
        }

        lastRefreshID = next;
        return next;
    }

    protected boolean isCancelled(int refreshID) {
        return !refreshList[refreshID];
    }

    protected abstract boolean refresh(int refreshID);
}
