package net.legacylauncher.ui.frames;

import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableLabel;
import net.legacylauncher.ui.swing.MagnifiedInsets;
import net.legacylauncher.ui.swing.editor.EditorPane;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.ui.swing.extended.VPanel;
import net.legacylauncher.util.SwingUtil;

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
        holder.setInsets(new MagnifiedInsets(25, 25, 25, 25));
        add(holder);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                holder.setPreferredSize(getRootPane().getSize());
                bodyText.setMinimumSize(new Dimension(Integer.MAX_VALUE, 1));
            }
        });

        head = new LocalizableLabel();
        head.setFont(head.getFont().deriveFont(head.getFont().getSize2D() + 3.f).deriveFont(Font.BOLD));
        //head.setForeground(new Color(head.getForeground().getRed(), head.getForeground().getGreen(), head.getForeground().getBlue(), 128));
        head.setIconTextGap(SwingUtil.magnify(20));
        holder.setNorth(head);

        body = new VPanel();
        body.setInsets(20, 0, 20, 0);
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
