package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RefreshButton extends LocalizableButton implements Blockable, ComponentManagerListener, ElyManagerListener {
    private static final int TYPE_REFRESH = 0;
    private static final int TYPE_CANCEL = 1;
    private LoginForm lf;
    private int type;
    private final ImageIcon refresh = Images.getScaledIcon("refresh.png", 16), cancel = Images.getScaledIcon("cancel.png", 16);

    private RefreshButton(LoginForm loginform, int type) {
        lf = loginform;
        setType(type, false);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onPressButton();
            }
        });

        TLauncher.getInstance().getManager().getComponent(ComponentManagerListenerHelper.class).addListener(this);
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
                TLauncher.getInstance().getManager().startAsyncRefresh();
                break;
            case 1:
                TLauncher.getInstance().getManager().stopRefresh();
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

    }

    public void unblock(Object reason) {
        if (reason.equals("refresh")) {
            setType(0);
        }

        setEnabled(true);
    }

    public void onElyUpdating(ElyManager manager) {
    }

    public void onElyUpdated(ElyManager manager) {
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
