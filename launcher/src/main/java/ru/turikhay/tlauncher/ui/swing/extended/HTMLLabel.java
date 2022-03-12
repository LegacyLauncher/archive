package ru.turikhay.tlauncher.ui.swing.extended;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class HTMLLabel extends ExtendedLabel {
    private String text;

    public HTMLLabel(String text, int labelWidth) {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }
        });
        setText(text, labelWidth);
    }

    public HTMLLabel(String text) {
        this(text, 0);
    }

    public HTMLLabel() {
        this(null);
    }

    private int labelWidth;

    public int getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(int width) {
        setText(text, width);
    }

    public void setText(String text, int labelWidth) {
        if (labelWidth < 0) {
            throw new IllegalArgumentException();
        }
        this.text = text;
        this.labelWidth = labelWidth;

        StringBuilder builder = new StringBuilder();

        builder.append("<html>");
        if (getLabelWidth() > 0) {
            builder.append("<div width=\"").append(getLabelWidth()).append("\">");
        }
        if (text != null) {
            builder.append(StringUtils.replace(text, "\n", "<br/>"));
        }
        if (getLabelWidth() > 0) {
            builder.append("</div>");
        }
        builder.append("</html>");

        String rawText = builder.toString();
        setRawText(rawText);
    }

    public void updateSize() {
        if (getLabelWidth() > 0) {
            Dimension d = SwingUtil.getPrefSize(this, getLabelWidth(), 0);
            setMinimumSize(d);
            setPreferredSize(d);
        }
    }
}
