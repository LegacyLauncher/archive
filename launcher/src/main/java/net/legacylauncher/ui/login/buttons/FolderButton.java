package net.legacylauncher.ui.login.buttons;

import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.ui.block.Unblockable;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.MinecraftUtil;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static net.legacylauncher.util.SwingUtil.updateUINullable;

public class FolderButton extends LocalizableButton implements Unblockable {
    private final LoginForm lf;

    final JPopupMenu menu = new JPopupMenu();
    final LocalizableMenuItem openFamily, openRoot, openMods;

    {
        menu.add(openFamily = LocalizableMenuItem.newItem("loginform.button.folder.family", e -> {
            File folder = getFamilyFolder();
            if (folder != null) {
                openFolder(folder);
            }
        }));

        menu.add(openRoot = LocalizableMenuItem.newItem("loginform.button.folder.root", e -> openDefFolder()));

        menu.add(openMods = LocalizableMenuItem.newItem("loginform.button.folder.mods", e -> openModsFolder()));
    }

    FolderButton(LoginForm loginform) {
        lf = loginform;
        setToolTipText("loginform.button.folder");
        setIcon(Images.getIcon24("folder-open"));

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.removeAll();

                CompleteVersion complete = getSelectedVersion();
                if (complete == null) {
                    openDefFolder();
                    return;
                }
                if (getFamilyFolder() != null) {
                    menu.add(openFamily);
                    openFamily.setVariables(complete.getFamily());
                }
                String id = complete.getID().toLowerCase(Locale.ROOT);
                if (id.contains("forge") || id.contains("fabric")) {
                    menu.add(openMods);
                }
                menu.add(openRoot);
                menu.show(FolderButton.this, 0, getHeight());
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

    private void openModsFolder() {
        CompleteVersion v = getSelectedVersion();
        if (v == null) {
            return;
        }
        File rootFolder = getFamilyFolder();
        if (rootFolder == null) {
            rootFolder = MinecraftUtil.getWorkingDirectory(false);
        }
        openFolder(new File(rootFolder, "mods"));
    }

    @Override
    public void updateUI() {
        updateUINullable(menu);
        super.updateUI();
    }
}
