package net.legacylauncher.ui.versions;

import net.legacylauncher.managers.VersionManager;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.swing.extended.ExtendedButton;
import net.minecraft.launcher.updater.VersionSyncInfo;

import javax.swing.*;
import java.util.List;

import static net.legacylauncher.util.SwingUtil.updateUINullable;

public class VersionRefreshButton extends ExtendedButton implements VersionHandlerListener, Blockable {
    private static final long serialVersionUID = -7148657244927244061L;
    private static final String PREFIX = "version.manager.refresher.";
    private static final String MENU = "version.manager.refresher.menu.";
    final VersionHandler handler;
    private final JPopupMenu menu;
    private VersionRefreshButton.ButtonState state;

    VersionRefreshButton(VersionList list) {
        handler = list.handler;
        menu = new JPopupMenu();
        LocalizableMenuItem local = new LocalizableMenuItem("version.manager.refresher.menu.local");
        local.addActionListener(e -> handler.refresh());
        menu.add(local);
        LocalizableMenuItem remote = new LocalizableMenuItem("version.manager.refresher.menu.remote");
        remote.addActionListener(e -> handler.asyncRefresh());
        menu.add(remote);
        addActionListener(e -> onPressed());
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

    @Override
    public void updateUI() {
        updateUINullable(menu);
        super.updateUI();
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
