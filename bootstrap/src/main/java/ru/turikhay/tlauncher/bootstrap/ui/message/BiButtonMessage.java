package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import java.awt.*;

public class BiButtonMessage extends TextMessage {
    private final Button yesButton, noButton;

    public BiButtonMessage(String question, Button yesButton, Button noButton) {
        super(question);
        this.yesButton = yesButton;
        this.noButton = noButton;
    }

    @Override
    void setupComponents(MessagePanel p) {
        super.setupComponents(p, true);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BorderLayout());
        buttons.add(yesButton.toSwingButton(), BorderLayout.WEST);
        buttons.add(noButton.toSwingButton(), BorderLayout.EAST);

        p.add(buttons, BorderLayout.SOUTH);
    }
}
