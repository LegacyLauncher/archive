package net.legacylauncher.ui.alert;

import net.legacylauncher.ui.swing.ScrollPane;
import net.legacylauncher.ui.swing.TextPopup;
import net.legacylauncher.ui.swing.editor.EditorPane;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.ui.swing.extended.ExtendedTextArea;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

class AlertPanel extends ExtendedPanel {

    AlertPanel(String text, Object content) {
        final int width = SwingUtil.magnify(600);

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;
        c.gridy = -1;

        ++c.gridy;
        add(Box.createRigidArea(new Dimension(width, 1)), c);

        EditorPane label = new EditorPane("text/html", "<html><div width=\"" + width + "\">" + StringUtils.replace(text, "\n", "<br/>") + "</div></html>");
        ++c.gridy;
        add(label, c);

        if (content != null) {
            ++c.gridy;
            add(Box.createRigidArea(new Dimension(width, 5)), c);

            String strContent = U.toLog(content);

            ExtendedTextArea textarea = new ExtendedTextArea();
            textarea.setWrapStyleWord(false);
            textarea.setLineWrap(true);
            textarea.setText(strContent);
            textarea.addMouseListener(new TextPopup());
            textarea.setEditable(false);

            final ScrollPane scroll = new ScrollPane(textarea, true);
            scroll.setPreferredSize(getPrefSize(strContent, width, width / 2, textarea.getFontMetrics(textarea.getFont()), textarea.getInsets()));

            ++c.gridy;
            add(scroll, c);
        }
    }

    static Dimension getPrefSize(String str, int prefWidth, int maxHeight, FontMetrics metrics, Insets insets) {
        if (str == null || str.isEmpty()) {
            return new Dimension(0, 0);
        }

        final int len = str.length();
        int lines = 1, lineWidth = 0;

        for (int pos = 0; pos < len; ++pos) {
            char c = str.charAt(pos);
            lineWidth += metrics.charWidth(c);

            if (lineWidth > prefWidth || c == '\n' || (c == '\r' && pos + 1 < len && str.charAt(pos + 1) == '\n' && ++pos > 0)) {
                ++lines;
                lineWidth = 0;
            }
        }

        int height = (metrics.getHeight() * lines) + SwingUtil.magnify(2);
        if (height > maxHeight) {
            height = maxHeight;
        }
        height += insets.top + insets.bottom;

        return new Dimension(prefWidth, height);
    }
}
