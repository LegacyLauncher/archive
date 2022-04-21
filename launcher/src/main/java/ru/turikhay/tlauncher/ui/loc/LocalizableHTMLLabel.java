package ru.turikhay.tlauncher.ui.loc;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LocalizableHTMLLabel extends LocalizableLabel {
    public LocalizableHTMLLabel(String path, Object... vars) {
        super(path, vars);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }
        });
    }

    public LocalizableHTMLLabel(String path) {
        this(path, Localizable.EMPTY_VARS);
    }

    public LocalizableHTMLLabel() {
        this(null);
    }

    public LocalizableHTMLLabel(int horizontalAlignment) {
        this(null);
        setHorizontalAlignment(horizontalAlignment);
    }

    private int labelWidth;

    public int getLabelWidth() {
        return labelWidth;
    }

    public LocalizableHTMLLabel setLabelWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException();
        }
        labelWidth = width;
        setText(path, (Object[]) variables);
        return this;
    }

    public void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);

        StringBuilder builder = new StringBuilder();

        builder.append("<html>");
        if (getLabelWidth() > 0) {
            builder.append("<div width=\"").append(getLabelWidth()).append("\">");
        }
        if (path != null) {
            builder.append(StringUtils.replace(Localizable.get(path, vars), "\n", "<br/>"));
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
