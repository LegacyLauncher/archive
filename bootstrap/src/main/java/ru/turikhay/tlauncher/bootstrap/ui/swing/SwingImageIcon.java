package ru.turikhay.tlauncher.bootstrap.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public interface SwingImageIcon {
    SwingImageIcon INSTANCE = new SwingImageIconImpl();

    static ImageIcon loadIcon(URL url, int width, int height) {
        return INSTANCE._loadIcon(url, width, height);
    }

    static Image loadImage(URL url) {
        return Toolkit.getDefaultToolkit().createImage(url);
    }

    ImageIcon _loadIcon(URL url, int width, int height);
}
