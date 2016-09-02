package ru.turikhay.tlauncher.ui.login.buttons;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.*;
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
        menu.add(openFamily = LocalizableMenuItem.newItem("loginform.button.folder.family", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File folder = getFamilyFolder();
                if(folder != null) {
                    openFolder(folder);
                }
            }
        }));

        menu.add(openRoot = LocalizableMenuItem.newItem("loginform.button.folder.root", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDefFolder();
            }
        }));
    }

    FolderButton(LoginForm loginform) {
        lf = loginform;
        setToolTipText("loginform.button.folder");
        setIcon(Images.getIcon("folder.png", SwingUtil.magnify(16), SwingUtil.magnify(16)));

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(getFamilyFolder() == null) {
                    openDefFolder();
                } else {
                    menu.removeAll();

                    CompleteVersion complete = getSelectedVersion();
                    if(complete != null) {
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
        return syncInfo == null? null : syncInfo.getLocalCompleteVersion();
    }

    private File getFamilyFolder() {
        if(!lf.global.getBoolean("minecraft.gamedir.separate")) {
            U.log("separate folders are off");
            return null;
        }

        CompleteVersion complete = getSelectedVersion();
        if(complete == null || StringUtils.isEmpty(complete.getFamily())) {
            return null;
        }

        return new File(MinecraftUtil.getWorkingDirectory(false), "home/" + complete.getFamily());
    }

    private void openFolder(final File folder) {
        if(folder == null) {
            throw new NullPointerException();
        }

        if(!folder.isDirectory()) {
            try {
                FileUtil.createFolder(folder);
            } catch(IOException ioE) {
                throw new RuntimeException(ioE);
            }
        }

        AsyncThread.execute(new Runnable() {
            @Override
            public void run() {
                OS.openFolder(folder);
            }
        });
    }

    private void openDefFolder() {
        openFolder(MinecraftUtil.getWorkingDirectory(false));
    }
}
