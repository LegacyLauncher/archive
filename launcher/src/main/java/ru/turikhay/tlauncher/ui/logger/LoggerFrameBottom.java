package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class LoggerFrameBottom extends BorderPanel implements LocalizableComponent {
    private final LoggerFrame frame;
    public final LocalizableButton closeCancelButton;
    public final ExtendedButton folder, save, pastebin, kill;

    File openFolder;

    LoggerFrameBottom(LoggerFrame fr) {
        frame = fr;
        setOpaque(true);
        setBackground(Color.darkGray);
        closeCancelButton = new LocalizableButton("logger.close.cancel");
        closeCancelButton.setVisible(false);
        closeCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (closeCancelButton.isVisible()) {
                    frame.hiding = false;
                    closeCancelButton.setVisible(false);
                }
            }
        });
        setCenter(closeCancelButton);
        folder = newButton("folder-open.png", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openFolder(openFolder == null ? MinecraftUtil.getWorkingDirectory() : openFolder);
            }
        });
        //folder.setEnabled(TLauncher.getInstance().getSettings().get("logger").equals(frame.logger.getName()));
        save = newButton("save.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.logger.saveAs();
            }
        });
        pastebin = newButton("mail-attachment.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.logger.sendPaste();
            }
        });
        kill = newButton("stop-circle-o.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.logger.launcher.killProcess();
                kill.setEnabled(false);
            }
        });
        kill.setEnabled(false);
        updateLocale();
        ExtendedPanel buttonPanel = new ExtendedPanel();
        buttonPanel.add(folder, save, pastebin, kill);
        setEast(buttonPanel);
    }

    private ExtendedButton newButton(String path, ActionListener action) {
        ExtendedButton button = new ExtendedButton();
        button.addActionListener(action);
        button.setIcon(Images.getIcon(path, SwingUtil.magnify(22), SwingUtil.magnify(22)));
        button.setPreferredSize(new Dimension(SwingUtil.magnify(32), SwingUtil.magnify(32)));
        return button;
    }

    public void updateLocale() {
        save.setToolTipText(Localizable.get("logger.save"));
        pastebin.setToolTipText(Localizable.get("logger.pastebin"));
        kill.setToolTipText(Localizable.get("logger.kill"));
    }
}
