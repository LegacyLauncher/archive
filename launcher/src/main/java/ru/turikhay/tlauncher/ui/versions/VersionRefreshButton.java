package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class VersionRefreshButton extends ExtendedButton implements VersionHandlerListener, Blockable {
    private static final long serialVersionUID = -7148657244927244061L;
    private static final String PREFIX = "version.manager.refresher.";
    private static final String MENU = "version.manager.refresher.menu.";
    final VersionHandler handler;
    private final JPopupMenu menu;
    private final LocalizableMenuItem local;
    private final LocalizableMenuItem remote;
    private VersionRefreshButton.ButtonState state;

    VersionRefreshButton(VersionList list) {
        handler = list.handler;
        menu = new JPopupMenu();
        local = new LocalizableMenuItem("version.manager.refresher.menu.local");
        local.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.refresh();
            }
        });
        menu.add(local);
        remote = new LocalizableMenuItem("version.manager.refresher.menu.remote");
        remote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.asyncRefresh();
            }
        });
        menu.add(remote);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onPressed();
            }
        });
        setState(VersionRefreshButton.ButtonState.REFRESH);
        handler.addListener(this);
    }

    void onPressed() {
        switch (state) {
            case REFRESH:
                menu.show(this, 0, getHeight());
                break;
            case CANCEL:
                handler.stopRefresh();
        }

    }

    private void setState(VersionRefreshButton.ButtonState state) {
        if (state == null) {
            throw new NullPointerException();
        } else {
            this.state = state;
            setIcon(Images.getIcon24(state.image));
        }
    }

    public void onVersionRefreshing(VersionManager vm) {
        setState(VersionRefreshButton.ButtonState.CANCEL);
    }

    public void onVersionRefreshed(VersionManager vm) {
        setState(VersionRefreshButton.ButtonState.REFRESH);
    }

    public void onVersionSelected(List<VersionSyncInfo> versions) {
    }

    public void onVersionDeselected() {
    }

    public void onVersionDownload(List<VersionSyncInfo> list) {
    }

    public void block(Object reason) {
        if (!reason.equals("refresh")) {
            setEnabled(false);
        }

    }

    public void unblock(Object reason) {
        setEnabled(true);
    }

    enum ButtonState {
        REFRESH("refresh"),
        CANCEL("stop-circle-o");

        final String image;

        ButtonState(String image) {
            this.image = image;
        }
    }
}
