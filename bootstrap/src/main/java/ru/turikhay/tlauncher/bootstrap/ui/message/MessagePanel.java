package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

class MessagePanel extends JPanel {
    private final Message message;
    private MessageWindow window;

    MessagePanel(Message message) {
        this.message = message;
        initPanel();
    }

    Message getMessage() {
        return message;
    }

    MessageHost getHost() {
        return window.getHost();
    }

    void notifyOpened(MessageWindow window) {
        this.window = window;
        message.messageShown(this);
    }

    void notifyClosed() {
        message.messageClosed(this);
    }

    private void initPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        message.setupComponents(this);
    }
}
