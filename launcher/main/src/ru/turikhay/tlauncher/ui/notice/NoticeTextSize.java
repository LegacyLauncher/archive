package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

class NoticeTextSize {
    private static final ExecutorService computators = Executors.newFixedThreadPool(2);

    private final Notice notice;
    private final Map<Float, Future<Dimension>> pending = new HashMap<Float, Future<Dimension>>();
    private final Map<Float, Dimension> byFontMap = new HashMap<Float, Dimension>();

    NoticeTextSize(Notice notice) {
        this.notice = U.requireNotNull(notice, "notice");
    }

    Notice getNotice() {
        return notice;
    }

    Dimension get(float size) {
        Future<Dimension> future;

        synchronized (pending) {
            Dimension result = byFontMap.get(size);
            if (result != null) {
                return result;
            }
            future = pend(size);
        }

        Dimension d;

        try {
            d = future.get(3, TimeUnit.SECONDS);
        } catch(TimeoutException timeout) {
            Sentry.sendError(NoticeTextSize.class, "text size is computing for too long", timeout, DataBuilder.create("notice", notice));
            d = new Dimension(0, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        set(size, d);
        return d;
    }

    Future<Dimension> pend(float size) {
        synchronized (pending) {
            Future<Dimension> future = pending.get(size);
            if(future == null) {
                future = computators.submit(new SizeCalculator(this, size));
                pending.put(size, future);
            }
            return future;
        }
    }

    private void set(float size, Dimension d) {
        byFontMap.put(size, U.requireNotNull(d));
    }
}
