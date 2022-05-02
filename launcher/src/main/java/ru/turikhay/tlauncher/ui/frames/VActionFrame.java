package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class VActionFrame extends ActionFrame {
    private final LocalizableLabel head;

    private final BorderPanel holder;
    private final VPanel body;
    private final VActionBody bodyText;

    protected final int labelWidth;

    private final ExtendedPanel footer;

    public VActionFrame(int width) {
        this.labelWidth = width;

        setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        setIconImages(SwingUtil.getFavicons());

        holder = new BorderPanel();
        holder.setVgap(5);
        holder.setInsets(new MagnifiedInsets(10, 20, 20, 20));
        add(holder);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                holder.setPreferredSize(getRootPane().getSize());
                bodyText.setMinimumSize(new Dimension(Integer.MAX_VALUE, 1));
            }
        });

        head = new LocalizableLabel();
        head.setFont(head.getFont().deriveFont(head.getFont().getSize2D() + 18f).deriveFont(Font.BOLD));
        //head.setForeground(new Color(head.getForeground().getRed(), head.getForeground().getGreen(), head.getForeground().getBlue(), 128));
        head.setIconTextGap(SwingUtil.magnify(10));
        holder.setNorth(head);

        body = new VPanel();
        body.add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 4))));
        holder.setCenter(body);

        bodyText = new VActionBody();
        body.add(bodyText);

        footer = new ExtendedPanel();
        holder.setSouth(footer);
    }

    public VActionFrame() {
        this(500);
    }

    public final BorderPanel getHolder() {
        return holder;
    }

    public final LocalizableLabel getHead() {
        return head;
    }

    public final VPanel getBody() {
        return body;
    }

    public final VActionBody getBodyText() {
        return bodyText;
    }

    public final ExtendedPanel getFooter() {
        return footer;
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    @Override
    public void pack() {
        super.pack();
        bodyText.calcText();
    }

    public class VActionBody extends EditorPane {
        protected VActionBody() {
            setAlignmentX(LEFT_ALIGNMENT);
        }

        public void setText(String text) {
            super.setText(Localizable.get(text));
            calcText();
        }

        public void setText(String text, Object... vars) {
            super.setText(Localizable.get(text, vars));
            calcText();
        }

        public void calcText() {
            setPreferredSize(new Dimension(labelWidth, SwingUtil.getPrefHeight(this, labelWidth)));
        }
    }
}
