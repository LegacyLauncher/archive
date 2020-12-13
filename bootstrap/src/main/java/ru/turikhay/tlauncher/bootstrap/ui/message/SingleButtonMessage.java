package ru.turikhay.tlauncher.bootstrap.ui.message;

import java.awt.*;

public class SingleButtonMessage extends TextMessage {
    private final Button button;

    public SingleButtonMessage(String text, Button button) {
        super(text);
        this.button = button;
    }

    @Override
    void setupComponents(MessagePanel p) {
        super.setupComponents(p, true);

        p.add(button.toSwingButton(), BorderLayout.SOUTH);
    }
}
