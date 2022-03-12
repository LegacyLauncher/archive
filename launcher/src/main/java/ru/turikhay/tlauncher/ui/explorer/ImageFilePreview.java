package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.util.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class ImageFilePreview extends JComponent {
    private static final long serialVersionUID = -1465489971097254329L;
    private static final Cursor DEFAULT = Cursor.getDefaultCursor();
    private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    ImageIcon thumbnail = null;
    File file = null;

    public ImageFilePreview(JFileChooser fc) {
        setCursor(DEFAULT);
        setPreferredSize(new Dimension(200, 100));
        fc.addPropertyChangeListener(e -> {
            boolean update = false;
            String prop = e.getPropertyName();
            if ("directoryChanged".equals(prop)) {
                file = null;
                update = true;
            } else if ("SelectedFileChangedProperty".equals(prop)) {
                file = (File) e.getNewValue();
                update = true;
            }

            if (update) {
                thumbnail = null;
                if (isShowing()) {
                    loadImage();
                    repaint();
                }
            }

        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    if (file != null) {
                        OS.openFile(file);
                    }

                }
            }
        });
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            setCursor(DEFAULT);
        } else {
            ImageIcon tmpIcon = new ImageIcon(file.getPath());
            setCursor(HAND);
            if (tmpIcon.getIconWidth() > 190) {
                thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(190, -1, 1));
            } else {
                thumbnail = tmpIcon;
            }

        }
    }

    protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }

        if (thumbnail != null) {
            int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
            int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
            if (y < 0) {
                y = 0;
            }

            if (x < 10) {
                x = 10;
            }

            thumbnail.paintIcon(this, g, x, y);
        }

    }
}
