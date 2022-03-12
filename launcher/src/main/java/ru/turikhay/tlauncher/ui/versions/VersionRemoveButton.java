package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class VersionRemoveButton extends ExtendedButton implements VersionHandlerListener, Blockable {
    private static final long serialVersionUID = 427368162418879141L;
    private static final String ILLEGAL_SELECTION_BLOCK = "illegal-selection";
    private static final String PREFIX = "version.manager.delete.";
    private static final String ERROR = "version.manager.delete.error.";
    private static final String ERROR_TITLE = "version.manager.delete.error.title";
    private static final String MENU = "version.manager.delete.menu.";
    private final VersionHandler handler;
    private final JPopupMenu menu;
    private boolean libraries;

    VersionRemoveButton(VersionList list) {
        setIcon(Images.getIcon24("remove"));
        handler = list.handler;
        handler.addListener(this);
        menu = new JPopupMenu();
        LocalizableMenuItem onlyJar = new LocalizableMenuItem("version.manager.delete.menu.jar");
        onlyJar.addActionListener(e -> onChosen(false));
        menu.add(onlyJar);
        LocalizableMenuItem withLibraries = new LocalizableMenuItem("version.manager.delete.menu.libraries");
        withLibraries.addActionListener(e -> onChosen(true));
        menu.add(withLibraries);
        addActionListener(e -> onPressed());
    }

    void onPressed() {
        menu.show(this, 0, getHeight());
    }

    void onChosen(boolean removeLibraries) {
        libraries = removeLibraries;
        handler.thread.deleteThread.iterate();
    }

    void delete() {
        if (handler.selected != null) {
            LocalVersionList localList = handler.vm.getLocalList();
            List<Throwable> errors = new ArrayList<>();

            for (VersionSyncInfo title : handler.selected) {
                if (title.isInstalled()) {
                    try {
                        localList.deleteVersion(title.getID(), libraries);
                    } catch (Throwable var6) {
                        errors.add(var6);
                    }
                }
            }

            if (!errors.isEmpty()) {
                String title1 = Localizable.get("version.manager.delete.error.title");
                String message1 = Localizable.get("version.manager.delete.error." + (errors.size() == 1 ? "single" : "multiply"), errors);
                Alert.showError(title1, message1);
            }
        }

        handler.refresh();
    }

    public void onVersionRefreshing(VersionManager vm) {
    }

    public void onVersionRefreshed(VersionManager vm) {
    }

    public void onVersionSelected(List<VersionSyncInfo> versions) {
        boolean onlyRemote = true;

        for (VersionSyncInfo version : versions) {
            if (version.isInstalled()) {
                onlyRemote = false;
                break;
            }
        }

        Blocker.setBlocked(this, "illegal-selection", onlyRemote);
    }

    public void onVersionDeselected() {
        Blocker.block(this, "illegal-selection");
    }

    public void onVersionDownload(List<VersionSyncInfo> list) {
    }

    public void block(Object reason) {
        setEnabled(false);
    }

    public void unblock(Object reason) {
        setEnabled(true);
    }
}
