package net.legacylauncher.ui.logger;

import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedButton;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.SwingUtil;

import java.awt.*;

class BottomPanel extends BorderPanel {
    private final Button folder, save, kill;

    BottomPanel() {
        folder = new Button("folder-open");
        save = new Button("save");
        kill = new Button("stop-circle-o");

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
                if (action != null) {
                    action.run();
                }
            });
            setIcon(Images.getIcon24(path));
            setPreferredSize(new Dimension(SwingUtil.magnify(32), SwingUtil.magnify(32)));
            setEnabled(false);
        }

        void setAction(Runnable action) {
            this.action = action;
            this.setEnabled(action != null);
        }
    }
}
