package net.legacylauncher.ui.login.buttons;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.managers.*;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.util.SwingUtil;

import java.awt.*;

import static net.legacylauncher.util.SwingUtil.updateUINullable;

public class RefreshButton extends LocalizableButton implements Blockable, ComponentManagerListener, LibraryReplaceProcessorListener {
    private static final int TYPE_REFRESH = 0;
    private static final int TYPE_CANCEL = 1;
    private final LoginForm lf;
    private int type;
    private final ImageIcon
            refresh = Images.getIcon24("refresh"),
            cancel = Images.getIcon24("remove");

    private RefreshButton(LoginForm loginform, int type) {
        lf = loginform;
        setType(type, false);
        addActionListener(e -> onPressButton());

        LegacyLauncher.getInstance().getManager().getComponent(ComponentManagerListenerHelper.class).addListener(this);
    }

    RefreshButton(LoginForm loginform) {
        this(loginform, 0);
    }

    public Insets getInsets() {
        return SwingUtil.magnify(super.getInsets());
    }

    private void onPressButton() {
        switch (type) {
            case 0:
                LegacyLauncher.getInstance().getManager().startAsyncRefresh();
                break;
            case 1:
                LegacyLauncher.getInstance().getManager().stopRefresh();
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
        }

        lf.defocus();
    }

    void setType(int type) {
        setType(type, true);
    }

    void setType(int type, boolean repaint) {
        switch (type) {
            case 0:
                setIcon(refresh);
                setToolTipText("loginform.button.refresh");
                break;
            case 1:
                setIcon(cancel);
                setToolTipText("loginform.button.refresh-cancel");
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
        }

        this.type = type;
    }

    public void onComponentsRefreshing(ComponentManager manager) {
        Blocker.block(this, "refresh");
    }

    public void onComponentsRefreshed(ComponentManager manager) {
        Blocker.unblock(this, "refresh");
    }

    public void block(Object reason) {
        if (reason.equals("refresh")) {
            setType(1);
        } else {
            setEnabled(false);
        }
        repaint();
    }

    public void unblock(Object reason) {
        if (reason.equals("refresh")) {
            setType(0);
        }

        setEnabled(true);
        repaint();
    }

    @Override
    public void onLibraryReplaceRefreshing(LibraryReplaceProcessor manager) {
        Blocker.block(this, "library");
    }

    @Override
    public void onLibraryReplaceRefreshed(LibraryReplaceProcessor manager) {
        Blocker.unblock(this, "library");
    }

    @Override
    public void updateUI() {
        updateUINullable(refresh, cancel);
        super.updateUI();
    }

    private enum Status {
        OK,
        BAD,
        AWFUL
    }

    private static class StatusResponse {
        private RefreshButton.Status ely;
        private RefreshButton.Status mojang;
        private String nextUpdateTime;
        private String responseTime;
    }
}
