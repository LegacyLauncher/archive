package ru.turikhay.tlauncher.bootstrap.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;

class Alert {
    private static final String TITLE = "Alert";

    static void showError(String message, Object textarea) {
        show(JOptionPane.ERROR_MESSAGE, message, textarea);
    }

    static void showWarning(String message, Object textarea) {
        show(JOptionPane.WARNING_MESSAGE, message, textarea);
    }

    private static void show(int messageType, String message, Object textarea) {
        JOptionPane.showMessageDialog(null, new AlertPanel(message, textarea), TITLE, messageType);
    }

    private static class AlertPanel extends JPanel {
        static final int width = 600;

        AlertPanel(String text, Object content) {
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

                String strContent = content instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) content) : String.valueOf(content);

                JTextArea textarea = new JTextArea();
                textarea.addMouseListener(new TextPopup());
                textarea.setWrapStyleWord(false);
                textarea.setLineWrap(true);
                textarea.setText(strContent);
                textarea.setEditable(false);

                final JScrollPane scroll = new JScrollPane(textarea);
                scroll.setPreferredSize(getPrefSize(strContent, width, width / 2, textarea.getFontMetrics(textarea.getFont()), textarea.getInsets()));

                ++c.gridy;
                add(scroll, c);
            }
        }

        private static Dimension getPrefSize(String str, int prefWidth, int maxHeight, FontMetrics metrics, Insets insets) {
            if (str == null || str.length() == 0) {
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

            int height = (metrics.getHeight() * lines) + 2;
            if (height > maxHeight) {
                height = maxHeight;
            }
            height += insets.top + insets.bottom;

            return new Dimension(prefWidth, height);
        }
    }

    private Alert() {
    }
}
