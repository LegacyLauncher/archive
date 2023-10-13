package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.util.U;

import javax.swing.*;

public class ExtendedFrame extends JFrame {

    public void showAndWait() {
        showAtCenter();
        while (isDisplayable()) {
            U.sleepFor(100);
        }
    }

    public void showAtCenter() {
        setVisible(true);
        setLocationRelativeTo(null);
        requestFocus();
    }
}
