package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.util.U;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

class NoticeTextSize {
    private static final ExecutorService computators = Executors.newFixedThreadPool(2);

    private final Notice notice;
    private final Map<ParamPair, Future<Dimension>> pending = new HashMap<ParamPair, Future<Dimension>>();
    private final Map<ParamPair, Dimension> byPairMap = new HashMap<ParamPair, Dimension>();

    NoticeTextSize(Notice notice) {
        this.notice = U.requireNotNull(notice, "notice");
    }

    Notice getNotice() {
        return notice;
    }

    Dimension get(ParamPair param) {
        Future<Dimension> future;

        synchronized (pending) {
            Dimension result = byPairMap.get(param);
            if (result != null) {
                return result;
            }
            future = pend(param);
        }

        Dimension d;

        try {
            d = future.get(3, TimeUnit.SECONDS);
        } catch(TimeoutException timeout) {
            d = new Dimension(0, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        set(param, d);
        return d;
    }

    Future<Dimension> pend(ParamPair param) {
        synchronized (pending) {
            Future<Dimension> future = pending.get(param);
            if(future == null) {
                future = computators.submit(new SizeCalculator(this, param));
                pending.put(param, future);
            }
            return future;
        }
    }

    private void set(ParamPair param, Dimension d) {
        byPairMap.put(param, U.requireNotNull(d));
    }
}
