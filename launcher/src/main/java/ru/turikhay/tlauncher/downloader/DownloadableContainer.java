package ru.turikhay.tlauncher.downloader;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadableContainer {
    private final List<DownloadableContainerHandler> handlers = Collections.synchronizedList(new ArrayList());
    private final List<Throwable> errors = Collections.synchronizedList(new ArrayList());
    final List<Downloadable> list = Collections.synchronizedList(new ArrayList());
    private final AtomicInteger sum = new AtomicInteger();
    private boolean locked;
    private boolean aborted;

    public List<Downloadable> getList() {
        return Collections.unmodifiableList(list);
    }

    public void add(Downloadable d) {
        if (d == null) {
            throw new NullPointerException();
        } else {
            checkLocked();
            if (!list.contains(d)) {
                list.add(d);
                d.setContainer(this);
                sum.incrementAndGet();
            }
        }
    }

    public void addAll(Downloadable... ds) {
        if (ds == null) {
            throw new NullPointerException();
        } else {
            for (int i = 0; i < ds.length; ++i) {
                if (ds[i] == null) {
                    throw new NullPointerException("Downloadable at " + i + " is NULL!");
                }

                if (!list.contains(ds[i])) {
                    list.add(ds[i]);
                    ds[i].setContainer(this);
                    sum.incrementAndGet();
                }
            }

        }
    }

    public void addAll(Collection<Downloadable> coll) {
        if (coll == null) {
            throw new NullPointerException();
        } else {
            int i = -1;
            Iterator var4 = coll.iterator();

            while (var4.hasNext()) {
                Downloadable d = (Downloadable) var4.next();

                ++i;
                if (d == null) {
                    throw new NullPointerException("Downloadable at" + i + " is NULL!");
                }

                if (!list.contains(d)) {
                    list.add(d);
                    d.setContainer(this);
                    sum.incrementAndGet();
                }
            }

        }
    }

    public void addAll(DownloadableContainer c) {
        addAll(c.list);
    }

    public void addHandler(DownloadableContainerHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        } else {
            checkLocked();
            handlers.add(handler);
        }
    }

    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean isAborted() {
        return aborted;
    }

    void setLocked(boolean locked) {
        this.locked = locked;
    }

    void checkLocked() {
        if (locked) {
            throw new IllegalStateException("Downloadable is locked!");
        }
    }

    void onStart() {
        Iterator var2 = handlers.iterator();

        while (var2.hasNext()) {
            DownloadableContainerHandler handler = (DownloadableContainerHandler) var2.next();
            handler.onStart(this);
        }

    }

    void onComplete(Downloadable d) throws RetryDownloadException {
        Iterator var3 = handlers.iterator();

        DownloadableContainerHandler handler;
        while (var3.hasNext()) {
            handler = (DownloadableContainerHandler) var3.next();
            handler.onComplete(this, d);
        }

        if (sum.decrementAndGet() <= 0) {
            var3 = handlers.iterator();

            while (var3.hasNext()) {
                handler = (DownloadableContainerHandler) var3.next();
                handler.onFullComplete(this);
            }

        }
    }

    void onAbort(Downloadable d) {
        aborted = true;
        errors.add(d.getError());
        if (sum.decrementAndGet() <= 0) {
            Iterator var3 = handlers.iterator();

            while (var3.hasNext()) {
                DownloadableContainerHandler handler = (DownloadableContainerHandler) var3.next();
                handler.onAbort(this);
            }

        }
    }

    void onError(Downloadable d, Throwable e) {
        errors.add(e);
        Iterator var4 = handlers.iterator();

        while (var4.hasNext()) {
            DownloadableContainerHandler handler = (DownloadableContainerHandler) var4.next();
            handler.onError(this, d, e);
        }

    }

    public static void removeDuplicates(DownloadableContainer a, DownloadableContainer b) {
        if (a.locked) {
            throw new IllegalStateException("First conatiner is already locked!");
        } else if (b.locked) {
            throw new IllegalStateException("Second container is already locked!");
        } else {
            a.locked = true;
            b.locked = true;

            try {
                List aList = a.list;
                List bList = b.list;
                ArrayList deleteList = new ArrayList();
                Iterator var6 = aList.iterator();

                while (var6.hasNext()) {
                    Downloadable aDownloadable = (Downloadable) var6.next();
                    Iterator var8 = bList.iterator();

                    while (var8.hasNext()) {
                        Downloadable bDownloadable = (Downloadable) var8.next();
                        if (aDownloadable.equals(bDownloadable)) {
                            deleteList.add(bDownloadable);
                        }
                    }
                }

                bList.removeAll(deleteList);
            } finally {
                a.locked = false;
                b.locked = false;
            }
        }
    }

    public static void removeDuplicates(List<? extends DownloadableContainer> list) {
        if (list == null) {
            throw new NullPointerException();
        } else if (list.size() >= 2) {
            for (int i = 0; i < list.size() - 1; ++i) {
                for (int k = i + 1; k < list.size(); ++k) {
                    removeDuplicates(list.get(i), list.get(k));
                }
            }

        }
    }
}
