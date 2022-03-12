package ru.turikhay.tlauncher.ui.login;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.SwingVersionManagerListener;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.settings.JREComboBox;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.tlauncher.ui.swing.VersionCellRenderer;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class VersionComboBox extends ExtendedComboBox<VersionSyncInfo> implements Blockable, VersionManagerListener, LocalizableComponent, LoginForm.LoginProcessListener {
    private static final long serialVersionUID = -9122074452728842733L;
    static Account.AccountType showVersionForType;
    private static final VersionSyncInfo LOADING;
    private static final VersionSyncInfo EMPTY;
    private final VersionManager manager;
    private final LoginForm loginForm;
    private final SimpleComboBoxModel<VersionSyncInfo> model;
    private String selectedVersion;

    static {
        LOADING = VersionCellRenderer.LOADING;
        EMPTY = VersionCellRenderer.EMPTY;
    }

    VersionComboBox(LoginForm lf) {
        super(new VersionCellRenderer() {
            public Component getListCellRendererComponent(JList<? extends VersionSyncInfo> list, VersionSyncInfo value, int index, boolean isSelected, boolean cellHasFocus) {
                list.setFixedCellWidth(SwingUtil.magnify(180));
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            public Account.AccountType getShowVersionsFor() {
                return VersionComboBox.showVersionForType;
            }
        });
        loginForm = lf;
        model = getSimpleModel();
        manager = TLauncher.getInstance().getVersionManager();
        manager.addListener(new SwingVersionManagerListener(this));
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                loginForm.buttons.play.updateState();
                VersionSyncInfo selected = getVersion();
                if (selected != null) {
                    selectedVersion = selected.getID();
                    loginForm.global.setForcefully("login.version", selectedVersion, false);
                    loginForm.global.store();
                    setToolTipText(selectedVersion);
                }
                if(loginForm.scene.settingsForm.isLoaded()) {
                    ((JREComboBox) loginForm.scene.settingsForm.get().jre.getComponent())
                            .selectedVersionChanged(selected);
                    loginForm.scene.settingsForm.get().useSeparateDir.getComponent().repaint();
                }
            }
        });
        selectedVersion = lf.global.get("login.version");
    }

    public VersionSyncInfo getVersion() {
        if(loginForm.requestedVersion != null) {
            return loginForm.requestedVersion;
        }
        VersionSyncInfo selected = (VersionSyncInfo) getSelectedItem();
        return selected != null && !selected.equals(LOADING) && !selected.equals(EMPTY) ? selected : null;
    }

    public void logginingIn() throws LoginException {
        VersionSyncInfo selected = getVersion();
        if (selected == null) {
            throw new LoginWaitException("Version list is empty, refreshing", new LoginWaitException.LoginWaitTask() {
                public void runTask() throws LoginException {
                    manager.refresh();

                    if (getVersion() == null) {
                        if (loginForm.global.getBoolean("minecraft.versions.sub.remote"))
                            Alert.showLocError("versions.notfound");
                        else
                            Alert.showLocError("versions.notfound.disabled");
                    }

                    throw new LoginException("Giving user a second chance to choose correct version...");
                }
            });
        } else if (selected.hasRemote() && selected.isInstalled() && !selected.isUpToDate()) {
            if (!Alert.showLocQuestion("versions.found-update")) {
                try {
                    CompleteVersion e = manager.getLocalList().getCompleteVersion(selected.getLocal());
                    e.setUpdatedTime(U.getUTC().getTime());
                    manager.getLocalList().saveVersion(e);
                } catch (IOException var3) {
                    Alert.showLocError("versions.found-update.error");
                }

            } else {
                loginForm.checkbox.forceupdate.setSelected(true);
            }
        }
    }

    public void loginFailed() {
    }

    public void loginSucceed() {
    }

    public void updateLocale() {
        updateList(manager);
    }

    public void onVersionsRefreshing(VersionManager vm) {
        updateList(null, null);
    }

    public void onVersionsRefreshingFailed(VersionManager vm) {
        updateList(manager);
    }

    public void onVersionsRefreshed(VersionManager vm) {
        updateList(manager);
    }

    void updateList(VersionManager manager) {
        if (manager == null) {
            throw new NullPointerException();
        } else {
            updateList(manager.getVersions(), null);
        }
    }

    void updateList(List<VersionSyncInfo> list, String select) {
        if (select == null && selectedVersion != null) {
            select = selectedVersion;
        }

        removeAllItems();
        if (list == null) {
            addItem(LOADING);
        } else {
            if (list.isEmpty()) {
                addItem(EMPTY);
            } else {
                model.addElements(list);
                Iterator var4 = list.iterator();

                while (var4.hasNext()) {
                    VersionSyncInfo version = (VersionSyncInfo) var4.next();
                    if (select != null && version.getID().equals(select)) {
                        setSelectedItem(version);
                    }
                }
            }

        }
    }

    public void block(Object reason) {
        setEnabled(false);
    }

    public void unblock(Object reason) {
        setEnabled(true);
    }
}
