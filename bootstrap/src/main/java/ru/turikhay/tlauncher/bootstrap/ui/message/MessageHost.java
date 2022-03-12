package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

public class MessageHost {
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private final WindowAdapter listener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            // current window is closed
            closeLatch.countDown();
        }
    };

    private MessageWindow window;
    private String title;

    public void setTitle(final String title) {
        SwingUtilities.invokeLater(() -> {
            MessageHost.this.title = title;
            if (window != null) {
                window.setTitle(title);
            }
        });
    }

    public void showMessage(final Message message) {
        SwingUtilities.invokeLater(() -> setMessage(message));
    }

    public void waitForClose() throws InterruptedException {
        closeLatch.await();
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (window != null) {
                window.dispose();
            }
        });
    }

    private void setMessage(Message message) {
        final MessageWindow window = this.window;
        this.window = null;

        destroyOldWindow(window);
        createNewWindow(message);
    }

    private void destroyOldWindow(MessageWindow oldWindow) {
        if (oldWindow == null) {
            return;
        }
        oldWindow.removeWindowListener(listener);
        oldWindow.dispose();
    }

    private void createNewWindow(Message message) {
        MessagePanel panel = new MessagePanel(message);
        MessageWindow window = new MessageWindow(this, panel);
        window.addWindowListener(this.listener);
        this.window = window;
        window.setTitle(title);
        window.setVisible(true);
        window.requestFocus();
    }
}
