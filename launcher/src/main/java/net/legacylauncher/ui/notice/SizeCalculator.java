package net.legacylauncher.ui.notice;

import net.legacylauncher.util.SwingUtil;

import java.awt.*;
import java.util.function.Supplier;

class SizeCalculator implements Supplier<Dimension> {
    private static final int MIN_WIDTH = 300, MIN_HEIGHT = 48, MAX_WIDTH = 450;

    private final Notice notice;
    private final ParamPair param;

    SizeCalculator(NoticeTextSize parent, ParamPair param) {
        this.notice = parent.getNotice();
        this.param = param;
    }

    @Override
    public Dimension get() {
        final NoticeEditorPane pane = new NoticeEditorPane();
            pane.setNotice(notice, param);
            if (param.width > 0) {
                return pane.calcPreferredSize(param.width);
            } else {
                return pane.calcPreferredSize(SwingUtil.magnify(MIN_HEIGHT), SwingUtil.magnify(MIN_WIDTH), SwingUtil.magnify(MAX_WIDTH));
            }
    }
}
