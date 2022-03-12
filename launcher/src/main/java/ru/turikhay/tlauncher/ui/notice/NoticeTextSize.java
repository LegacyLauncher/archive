package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class NoticeTextSize {
    private final Notice notice;
    private final Map<ParamPair, Dimension> byPairMap = new HashMap<ParamPair, Dimension>();

    NoticeTextSize(Notice notice) {
        this.notice = U.requireNotNull(notice, "notice");
    }

    Notice getNotice() {
        return notice;
    }

    Dimension get(ParamPair param) {
        return SwingUtil.waitAndReturn(() -> {
            Dimension d = new SizeCalculator(this, param).get();
            set(param, d);
            return d;
        });
    }

    private void set(ParamPair param, Dimension d) {
        byPairMap.put(param, U.requireNotNull(d));
    }
}
