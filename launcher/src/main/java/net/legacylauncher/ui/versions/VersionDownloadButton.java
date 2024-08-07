package net.legacylauncher.ui.versions;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.downloader.AbortedDownloadException;
import net.legacylauncher.downloader.DownloadableContainer;
import net.legacylauncher.managers.VersionManager;
import net.legacylauncher.managers.VersionSyncInfoContainer;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.block.Unblockable;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.swing.extended.ExtendedButton;
import net.minecraft.launcher.updater.VersionSyncInfo;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.legacylauncher.util.SwingUtil.updateUINullable;

public class VersionDownloadButton extends ExtendedButton implements VersionHandlerListener, Unblockable {
    private static final String SELECTION_BLOCK = "selection";
    private static final String PREFIX = "version.manager.downloader.";
    private static final String WARNING = "version.manager.downloader.warning.";
    private static final String WARNING_TITLE = "version.manager.downloader.warning.title";
    private static final String WARNING_FORCE = "version.manager.downloader.warning.force.";
    private static final String ERROR = "version.manager.downloader.error.";
    private static final String ERROR_TITLE = "version.manager.downloader.error.title";
    private static final String INFO = "version.manager.downloader.info.";
    private static final String INFO_TITLE = "version.manager.downloader.info.title";
    private static final String MENU = "version.manager.downloader.menu.";
    final VersionHandler handler;
    final Blockable blockable;
    private final JPopupMenu menu;
    private VersionDownloadButton.ButtonState state;
    private boolean downloading;
    private boolean aborted;
    boolean forceDownload;

    VersionDownloadButton(VersionList list) {
        handler = list.handler;
        blockable = new Blockable() {
            public void block(Object reason) {
                setEnabled(false);
            }

            public void unblock(Object reason) {
                setEnabled(true);
            }
        };
        menu = new JPopupMenu();
        LocalizableMenuItem ordinary = new LocalizableMenuItem("version.manager.downloader.menu.ordinary");
        ordinary.addActionListener(e -> {
            forceDownload = false;
            onDownloadCalled();
        });
        menu.add(ordinary);
        LocalizableMenuItem force = new LocalizableMenuItem("version.manager.downloader.menu.force");
        force.addActionListener(e -> {
            forceDownload = true;
            onDownloadCalled();
        });
        menu.add(force);
        addActionListener(e -> onPressed());
        setState(VersionDownloadButton.ButtonState.DOWNLOAD);
        handler.addListener(this);
    }

    void setState(VersionDownloadButton.ButtonState state) {
        if (state == null) {
            throw new NullPointerException();
        } else {
            this.state = state;
            setIcon(Images.getIcon24(state.image));
        }
    }

    void onPressed() {
        switch (state) {
            case DOWNLOAD:
                onDownloadPressed();
                break;
            case STOP:
                onStopCalled();
        }

    }

    void onDownloadPressed() {
        menu.show(this, 0, getHeight());
    }

    void onDownloadCalled() {
        if (state != VersionDownloadButton.ButtonState.DOWNLOAD) {
            throw new IllegalStateException();
        } else {
            handler.thread.startThread.iterate();
        }
    }

    void onStopCalled() {
        if (state != VersionDownloadButton.ButtonState.STOP) {
            throw new IllegalStateException();
        } else {
            handler.thread.stopThread.iterate();
        }
    }

    void startDownload() {
        aborted = false;
        List<VersionSyncInfo> list = handler.getSelectedList();
        if (list != null && !list.isEmpty()) {
            int countLocal = 0;
            VersionSyncInfo local = null;

            for (VersionSyncInfo containers : list) {
                if (forceDownload) {
                    if (!containers.hasRemote()) {
                        Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.local", containers.getID()));
                        return;
                    }

                    if (containers.isUpToDate() && containers.isInstalled()) {
                        ++countLocal;
                        local = containers;
                    }
                }
            }

            if (countLocal > 0) {
                String var17 = Localizable.get("version.manager.downloader.warning.title");
                Object container;
                String var19;
                if (countLocal == 1) {
                    var19 = "single";
                    container = local.getID();
                } else {
                    var19 = "multiply";
                    container = countLocal;
                }

                if (!Alert.showQuestion(var17, Localizable.get("version.manager.downloader.warning.force." + var19, container))) {
                    return;
                }
            }

            List<VersionSyncInfoContainer> var18 = new ArrayList<>();
            VersionManager var20 = LegacyLauncher.getInstance().getVersionManager();

            try {
                downloading = true;

                for (VersionSyncInfo var21 : list) {
                    try {
                        var21.resolveCompleteVersion(var20, forceDownload);
                        VersionSyncInfoContainer errors = var20.downloadVersion(var21, new String[]{}, forceDownload);
                        if (aborted) {
                            return;
                        }

                        if (!errors.getList().isEmpty()) {
                            var18.add(errors);
                        }
                    } catch (Exception var15) {
                        Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.getting", var21.getID()), var15);
                        return;
                    }
                }

                if (var18.isEmpty()) {
                    Alert.showMessage(Localizable.get("version.manager.downloader.info.title"), Localizable.get("version.manager.downloader.info.no-needed"));
                    return;
                }

                if (var18.size() > 1) {
                    DownloadableContainer.removeDuplicates(var18);
                }

                if (aborted) {
                    return;
                }

                for (DownloadableContainer downloadableContainer : var18) {
                    handler.downloader.add(downloadableContainer);
                }

                handler.downloading = list;
                handler.onVersionDownload(list);
                handler.downloader.startDownloadAndWait();
            } finally {
                downloading = false;
            }

            handler.downloading.clear();

            for (VersionSyncInfoContainer var23 : var18) {
                List<Throwable> var24 = var23.getErrors();
                VersionSyncInfo version = var23.getVersion();
                if (var24.isEmpty()) {
                    try {
                        var20.getLocalList().saveVersion(version.getCompleteVersion(forceDownload));
                    } catch (IOException var14) {
                        Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.saving", version.getID()), var14);
                        return;
                    }
                } else if (!(var24.get(0) instanceof AbortedDownloadException)) {
                    Alert.showError(Localizable.get("version.manager.downloader.error.title"), Localizable.get("version.manager.downloader.error.downloading", version.getID()), var24);
                }
            }

            handler.refresh();
        }
    }

    void stopDownload() {
        aborted = true;
        if (downloading) {
            handler.downloader.stopDownloadAndWait();
        }

    }

    public void onVersionRefreshing(VersionManager vm) {
    }

    public void onVersionRefreshed(VersionManager vm) {
    }

    public void onVersionSelected(List<VersionSyncInfo> versions) {
        if (!downloading) {
            blockable.unblock("selection");
        }

    }

    public void onVersionDeselected() {
        if (!downloading) {
            blockable.block("selection");
        }

    }

    public void onVersionDownload(List<VersionSyncInfo> list) {
    }

    @Override
    public void updateUI() {
        updateUINullable(menu);
        super.updateUI();
    }

    public enum ButtonState {
        DOWNLOAD("download"),
        STOP("stop-circle-o");

        final String image;

        ButtonState(String image) {
            this.image = image;
        }
    }
}
