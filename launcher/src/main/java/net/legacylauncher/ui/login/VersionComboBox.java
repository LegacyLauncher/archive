package net.legacylauncher.ui.login;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.managers.SwingVersionManagerListener;
import net.legacylauncher.managers.VersionManager;
import net.legacylauncher.managers.VersionManagerListener;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.settings.JREComboBox;
import net.legacylauncher.ui.settings.MemorySlider;
import net.legacylauncher.ui.swing.SimpleComboBoxModel;
import net.legacylauncher.ui.swing.VersionCellRenderer;
import net.legacylauncher.ui.swing.combobox.ComboBoxFilter;
import net.legacylauncher.ui.swing.combobox.IconText;
import net.legacylauncher.ui.swing.extended.ExtendedComboBox;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionFamily;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class VersionComboBox extends ExtendedComboBox<VersionSyncInfo> implements Blockable, VersionManagerListener, LocalizableComponent, LoginForm.LoginProcessListener {
    private static final long serialVersionUID = -9122074452728842733L;
    static Account.AccountType showVersionForType = Account.AccountType.PLAIN;
    private static final VersionSyncInfo LOADING;
    private static final VersionSyncInfo EMPTY;
    private final VersionManager manager;
    private final LoginForm loginForm;
    private final SimpleComboBoxModel<VersionSyncInfo> model;
    private String selectedVersion;
    ComboBoxFilter<VersionSyncInfo> comboBoxFilter;
    private final VersionSeeker seeker = new VersionSeeker();
    private boolean ready;

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
        manager = LegacyLauncher.getInstance().getVersionManager();
        manager.addListener(new SwingVersionManagerListener(this));
        addItemListener(e -> {
            if (!ready) {
                return;
            }
            loginForm.buttons.play.updateState();
            VersionSyncInfo selected = getVersion();
            if (selected != null) {
                selectedVersion = selected.getID();
                loginForm.global.setForcefully("login.version", selectedVersion, false);
                loginForm.global.store();
                setToolTipText(selectedVersion);
            }
            if (loginForm.scene.settingsForm.isLoaded()) {
                ((JREComboBox) loginForm.scene.settingsForm.get().jre.getComponent())
                        .selectedVersionChanged(selected);
                loginForm.scene.settingsForm.get().useSeparateDir.getComponent().repaint();
                ((MemorySlider) loginForm.scene.settingsForm.get().memory.getComponent())
                        .updateForCurrentlySelectedVersion();
            }
        });
        selectedVersion = lf.global.get("login.version");
        initComboBoxFilter();
        addItem(LOADING);
        ready = true;
    }

    private void initComboBoxFilter() {
        if (comboBoxFilter != null) {
            comboBoxFilter.cleanUp();
        }
        comboBoxFilter = ComboBoxFilter.decorate(this,
                () -> versionList == null ? Collections.emptyList() : versionList,
                (vs) -> {
                    if (vs == null) {
                        return IconText.EMPTY;
                    }
                    ImageIcon imageIcon = null;
                    String text = VersionCellRenderer.getLabelFor(vs);
                    if (LegacyLauncher.getInstance().getLibraryManager()
                            .hasLibrariesExplicitly(vs, VersionComboBox.showVersionForType.toString())) {
                        imageIcon = VersionCellRenderer.getIconFor(VersionComboBox.showVersionForType);
                    }
                    return new IconText(imageIcon, text);
                },
                seeker
        );
    }

    public VersionSyncInfo getVersion() {
        if (loginForm.requestedVersion != null) {
            return loginForm.requestedVersion;
        }
        VersionSyncInfo selected = (VersionSyncInfo) getSelectedItem();
        return selected != null && !selected.equals(LOADING) && !selected.equals(EMPTY) ? selected : null;
    }

    public void loggingIn() throws LoginException {
        VersionSyncInfo selected = getVersion();
        if (selected == null) {
            throw new LoginWaitException("Version list is empty, refreshing", () -> {
                manager.refresh();

                if (getVersion() == null) {
                    if (loginForm.global.getBoolean("minecraft.versions.sub.remote"))
                        Alert.showLocError("versions.notfound");
                    else
                        Alert.showLocError("versions.notfound.disabled");
                }

                throw new LoginException("Giving user a second chance to choose correct version...");
            });
        } else if (selected.hasRemote() && selected.isInstalled() && !selected.isUpToDate() && !loginForm.checkbox.forceupdate.isSelected()) {
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
        seeker.updateLocale();
    }

    private List<VersionSyncInfo> versionList;

    void updateList(VersionManager manager) {
        if (manager == null) {
            throw new NullPointerException();
        } else {
            versionList = manager.getVersions(createFilter(), true);
            updateList(versionList, null);
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

                for (VersionSyncInfo version : list) {
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

    private Filter createFilter() {
        Configuration settings = LegacyLauncher.getInstance().getSettings();
        return new Filter(
                settings.getVersionFilter(),
                settings.getBoolean("minecraft.versions.only-installed") ?
                        manager.getVersions(true).stream()
                                .filter(VersionSyncInfo::isInstalled)
                                .map(VersionSyncInfo::getID)
                                .collect(Collectors.toList())
                        : null
        );
    }

    @Override
    public void updateUI() {
        if (ready) {
            initComboBoxFilter();
        }
        super.updateUI();
    }

    private static class Filter extends VersionFilter {
        final VersionFilter delegate;
        @Nullable
        final List<String> installed;

        Filter(VersionFilter delegate, @Nullable List<String> installed) {
            this.delegate = delegate;
            this.installed = installed;
        }

        @Override
        public boolean satisfies(Version v) {
            if (!delegate.satisfies(v)) {
                return false;
            }
            return installed == null || installed.contains(v.getID());
        }
    }

    private static class VersionSeeker implements BiPredicate<VersionSyncInfo, String>, LocalizableComponent {
        private Map<ReleaseType, String> localizedReleaseTypeCache;

        @Override
        public boolean test(VersionSyncInfo vs, String termRaw) {
            if (vs == null || vs.getID() == null) {
                return false;
            }
            String term = normalize(termRaw);
            String id = normalize(vs.getID().toLowerCase(Locale.ROOT));
            if (id.contains(term)) {
                return true;
            }
            VersionFamily.Guess familyGuess = VersionFamily.guessFamilyOf(vs);
            if (familyGuess != null && familyGuess.getFamily().toLowerCase(Locale.ROOT).contains(term)) {
                return true;
            }
            String localizedTypeName = localizedReleaseTypeCache.get(vs.getAvailableVersion().getReleaseType());
            if (localizedTypeName != null) {
                if (localizedTypeName.contains(term)) {
                    return true;
                }
                if ((localizedTypeName + " " + id).contains(term)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void updateLocale() {
            Map<ReleaseType, String> cache = new HashMap<>();
            ReleaseType.valuesCollection().forEach(type -> {
                String localizedTypeName = Localizable.nget("version." + type.name().toLowerCase(Locale.ROOT));
                if (localizedTypeName != null) {
                    cache.put(
                            type,
                            localizedTypeName.toLowerCase(Locale.ROOT)
                    );
                }
            });
            this.localizedReleaseTypeCache = cache;
        }

        private static String normalize(String str) {
            str = str.toLowerCase(Locale.ROOT);
            str = str.trim();
            str = StringUtils.replaceChars(str, ",/", ".");
            return str;
        }
    }
}
