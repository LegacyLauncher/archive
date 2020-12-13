package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class MessageWindow extends JFrame {
    private final MessageHost host;
    private final MessagePanel panel;

    MessageWindow(MessageHost host, MessagePanel panel) {
        this.host = host;
        this.panel = panel;
        initWindow();
    }

    MessageHost getHost() {
        return host;
    }

    private void initWindow() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel.notifyOpened(MessageWindow.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                panel.notifyClosed();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(panel);
        setMinimumSize(new Dimension(500, 1));
        setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
}
