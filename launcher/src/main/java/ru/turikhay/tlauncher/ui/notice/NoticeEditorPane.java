package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

class NoticeEditorPane extends EditorPane {

    NoticeEditorPane() {
    }

    void setNotice(Notice notice, ParamPair param) {
        setFont(getFont().deriveFont(param.fontSize));
        setText("<html><div "+ (param.width > 0? "width=\""+ param.width +"\" " : "") +"align=\"center\">" + notice.getText() + "</div></html>");
    }

    Dimension calcPreferredSize(final int minHeight, final int minWidth, final int maxWidth, int widthStep, int heightStep) {
        final int
                originWidth = SwingUtil.getPrefWidth(this, 0, widthStep),
                originHeight = SwingUtil.getPrefHeight(this, Integer.MAX_VALUE);
        int prefWidth = originWidth, prefHeight = originHeight;

        if (prefWidth > maxWidth) {
            prefHeight -= 1;
            do {
                prefWidth = SwingUtil.getPrefWidth(this, prefHeight += heightStep, widthStep);
            } while (prefWidth >= maxWidth);
        } else if (prefWidth < minWidth) {
            prefHeight += 1;
            do {
                prefWidth = SwingUtil.getPrefWidth(this, prefHeight -= heightStep, widthStep);
            } while (prefWidth > maxWidth && prefWidth >= originWidth && prefHeight >= minHeight);
        }

        return new Dimension(prefWidth, prefHeight);
    }

    Dimension calcPreferredSize(final int minHeight, final int minWidth, final int maxWidth) {
        return calcPreferredSize(minHeight, minWidth, maxWidth, 5, 2);
    }

    Dimension calcPreferredSize(int width) {
        return new Dimension(width, SwingUtil.getPrefHeight(this, width));
    }
}
