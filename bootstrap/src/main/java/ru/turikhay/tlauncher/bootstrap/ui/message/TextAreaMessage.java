package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import java.awt.*;

public class TextAreaMessage extends TextMessage {
    private final String content;

    public TextAreaMessage(String text, String content) {
        super(text);
        this.content = content;
    }

    @Override
    void setupComponents(MessagePanel p) {
        super.setupComponents(p, true);

        JTextArea textArea = new JTextArea(content);
        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        p.add(scrollPane, BorderLayout.SOUTH);
    }
}
