package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.util.U;

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
