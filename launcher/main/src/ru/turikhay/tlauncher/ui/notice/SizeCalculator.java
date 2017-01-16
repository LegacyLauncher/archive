package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.util.Factory;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.WeakObjectPool;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

class SizeCalculator implements Callable<Dimension> {
    private static final int MIN_WIDTH = 300, MIN_HEIGHT = 48, MAX_WIDTH = 500;
    private static final WeakObjectPool<NoticeEditorPane> editorPanePool = new WeakObjectPool<NoticeEditorPane>(new Factory<NoticeEditorPane>() {
        @Override
        public NoticeEditorPane createNew() {
            return new NoticeEditorPane();
        }
    });

    private final Notice notice;
    private final float fontSize;

    SizeCalculator(NoticeTextSize parent, float fontSize) {
        this.notice = parent.getNotice();
        this.fontSize = fontSize;
    }

    @Override
    public Dimension call() throws Exception {
        WeakObjectPool<NoticeEditorPane>.ObjectRef<NoticeEditorPane> ref = editorPanePool.get();
        try {
            final NoticeEditorPane pane = ref.get();
            pane.setNotice(notice, fontSize);
            return pane.calcPreferredSize(SwingUtil.magnify(MIN_HEIGHT), SwingUtil.magnify(MIN_WIDTH), SwingUtil.magnify(MAX_WIDTH));
        } finally {
            ref.free();
        }
    }
}
