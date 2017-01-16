package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

class NoticeEditorPane extends EditorPane {

    NoticeEditorPane() {
    }

    void setNotice(Notice notice, float fontSize) {
        setFont(getFont().deriveFont(fontSize));
        setText("<html><div align=\"center\">" + notice.getText() + "</div></html>");
    }

    Dimension calcPreferredSize(final int minHeight, final int minWidth, final int maxWidth) {
        final int
                originWidth = SwingUtil.getPrefWidth(this, 0),
                originHeight = SwingUtil.getPrefHeight(this, Integer.MAX_VALUE);
        int prefWidth = originWidth, prefHeight = originHeight;

        if (prefWidth > maxWidth) {
            prefHeight -= 1;
            do {
                prefWidth = SwingUtil.getPrefWidth(this, prefHeight += 1);
            } while (prefWidth >= maxWidth);
        } else if (prefWidth < minWidth) {
            prefHeight += 1;
            do {
                prefWidth = SwingUtil.getPrefWidth(this, prefHeight -= 1);
            } while (prefWidth > maxWidth && prefWidth >= originWidth && prefHeight >= minHeight);
        }

        return new Dimension(prefWidth, prefHeight);
    }
}
