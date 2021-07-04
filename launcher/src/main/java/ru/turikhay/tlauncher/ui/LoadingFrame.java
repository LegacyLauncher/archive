package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class LoadingFrame extends JFrame {
    private final ProgressBar progress;

    public LoadingFrame() {
        SwingUtil.initLookAndFeel();
        setLayout(new BorderLayout());
        progress = new ProgressBar();
        progress.setPreferredSize(new Dimension(250, 0));
        add(progress, "Center");
        add(new JLabel(Images.getIcon("fav32.png")), "West");
        setType(Type.UTILITY);

        pack();
        setResizable(false);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
    }

    public ProgressBar getProgressBar() {
        return progress;
    }

    public void setProgress(int percent) {
        progress.setIndeterminate(false);
        progress.setValue(percent);
    }
}
