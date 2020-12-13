package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.util.function.Supplier;

class BottomPanel extends BorderPanel {
    private final Button folder, save, kill;

    BottomPanel() {
        folder = new Button("folder-open.png");
        save = new Button("save.png");
        kill = new Button("stop-circle-o.png");

        ExtendedPanel buttonPanel = new ExtendedPanel();
        buttonPanel.add(folder, save, kill);
        setEast(buttonPanel);

        setOpaque(true);
        setBackground(Color.darkGray);
    }

    void setFolder(Runnable action) {
        folder.setAction(action);
    }

    void setSave(Runnable action) {
        save.setAction(action);
    }

    void setKill(Runnable action) {
        kill.setAction(action);
    }

    private static class Button extends ExtendedButton {
        private Runnable action;

        Button(String path) {
            addActionListener((e) -> {
                if(action != null) {
                    action.run();
                }
            });
            setIcon(Images.getIcon(path, SwingUtil.magnify(22), SwingUtil.magnify(22)));
            setPreferredSize(new Dimension(SwingUtil.magnify(32), SwingUtil.magnify(32)));
            setEnabled(false);
        }

        void setAction(Runnable action) {
            this.action = action;
            this.setEnabled(action != null);
        }
    }
}
