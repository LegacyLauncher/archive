package net.legacylauncher.ui.notice;

import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.WeakObjectPool;

import java.awt.*;
import java.util.function.Supplier;

class SizeCalculator implements Supplier<Dimension> {
    private static final int MIN_WIDTH = 300, MIN_HEIGHT = 48, MAX_WIDTH = 450;
    private static final WeakObjectPool<NoticeEditorPane> editorPanePool = new WeakObjectPool<>(NoticeEditorPane::new);

    private final Notice notice;
    private final ParamPair param;

    SizeCalculator(NoticeTextSize parent, ParamPair param) {
        this.notice = parent.getNotice();
        this.param = param;
    }

    @Override
    public Dimension get() {
        WeakObjectPool.ObjectRef<NoticeEditorPane> ref = editorPanePool.get();
        try {
            final NoticeEditorPane pane = ref.get();
            pane.setNotice(notice, param);
            if (param.width > 0) {
                return pane.calcPreferredSize(param.width);
            } else {
                return pane.calcPreferredSize(SwingUtil.magnify(MIN_HEIGHT), SwingUtil.magnify(MIN_WIDTH), SwingUtil.magnify(MAX_WIDTH));
            }
        } finally {
            ref.free();
        }
    }
}
