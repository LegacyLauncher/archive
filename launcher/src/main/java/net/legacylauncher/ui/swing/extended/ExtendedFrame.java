package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.LegacyLauncher;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

public class ExtendedFrame extends JFrame {

    public void showAndWait() {
        CountDownLatch latch = new CountDownLatch(1);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                removeWindowListener(this);
                latch.countDown();
            }
        });
        showAtCenter();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void showAtCenter() {
        JFrame frame = null;
        if (LegacyLauncher.getInstance() != null) {
            frame = LegacyLauncher.getInstance().getFrame();
        }
        setLocationRelativeTo(frame);
        setVisible(true);
        requestFocus();
    }
}
