package ru.turikhay.tlauncher.bootstrap.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SwingImageIconImpl implements SwingImageIcon {
    @Override
    public ImageIcon _loadIcon(URL url, int width, int height) {
        return new ImageIcon(
                Toolkit.getDefaultToolkit()
                        .createImage(url)
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)
        );
    }
}
