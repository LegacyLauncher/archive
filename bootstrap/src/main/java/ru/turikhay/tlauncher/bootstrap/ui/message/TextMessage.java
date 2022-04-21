package ru.turikhay.tlauncher.bootstrap.ui.message;

import ru.turikhay.tlauncher.bootstrap.ui.swing.HTMLPane;

import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextMessage extends Message {
    private final String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    void setupComponents(MessagePanel p) {
        setupComponents(p, false);
    }

    void setupComponents(MessagePanel p, boolean useBottomBorder) {
        HTMLPane htmlPane = new HTMLPane();
        htmlPane.setText(toHtml(text));

        if (useBottomBorder) {
            htmlPane.setBorder(new EmptyBorder(0, 0, 30, 0));
        }

        p.setLayout(new BorderLayout());
        p.add(htmlPane, BorderLayout.CENTER);
    }

    private static String toHtml(String text) {
        return "<!DOCTYPE html><html>"
                + "<head>"
                //+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
                + "<style type=\"text/css\">body { width: 500px; }</style>"
                + "</head>"
                + "<body>"
                + text
                + "</body>"
                + "</html>";
    }
}
