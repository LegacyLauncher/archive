package ru.turikhay.tlauncher.ui.login.buttons;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class FolderButton extends LocalizableButton implements Unblockable {
    private final LoginForm lf;

    final JPopupMenu menu = new JPopupMenu();
    final LocalizableMenuItem openFamily, openRoot;

    {
        menu.add(openFamily = LocalizableMenuItem.newItem("loginform.button.folder.family", e -> {
            File folder = getFamilyFolder();
            if (folder != null) {
                openFolder(folder);
            }
        }));

        menu.add(openRoot = LocalizableMenuItem.newItem("loginform.button.folder.root", e -> openDefFolder()));
    }

    FolderButton(LoginForm loginform) {
        lf = loginform;
        setToolTipText("loginform.button.folder");
        setIcon(Images.getIcon24("folder-open"));

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFamilyFolder() == null) {
                    openDefFolder();
                } else {
                    menu.removeAll();

                    CompleteVersion complete = getSelectedVersion();
                    if (complete != null) {
                        menu.add(openFamily);
                        openFamily.setVariables(complete.getFamily());
                    }

                    menu.add(openRoot);
                    menu.show(FolderButton.this, 0, getHeight());
                }
            }
        });
    }

    public Insets getInsets() {
        return SwingUtil.magnify(super.getInsets());
    }

    private CompleteVersion getSelectedVersion() {
        VersionSyncInfo syncInfo = lf.versions.getVersion();
        return syncInfo == null ? null : syncInfo.getLocalCompleteVersion();
    }

    private File getFamilyFolder() {
        Configuration.SeparateDirs separateDirs = lf.global.getSeparateDirs();

        CompleteVersion complete = getSelectedVersion();
        if (complete == null) {
            return null;
        }

        String dirName = null;
        switch (separateDirs) {
            case FAMILY:
                dirName = complete.getFamily();
                break;
            case VERSION:
                dirName = complete.getID();
                break;
        }

        if (dirName != null && !StringUtils.isEmpty(dirName))
            return new File(MinecraftUtil.getWorkingDirectory(false), "home/" + dirName);
        else return null;
    }

    private void openFolder(final File folder) {
        if (folder == null) {
            throw new NullPointerException();
        }

        if (!folder.isDirectory()) {
            try {
                FileUtil.createFolder(folder);
            } catch (IOException ioE) {
                throw new RuntimeException(ioE);
            }
        }

        AsyncThread.execute(() -> OS.openFolder(folder));
    }

    private void openDefFolder() {
        openFolder(MinecraftUtil.getWorkingDirectory(false));
    }
}
