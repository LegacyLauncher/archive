package ru.turikhay.tlauncher.ui.settings;

import net.minecraft.launcher.versions.ReleaseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.managers.VersionLists;
import ru.turikhay.tlauncher.minecraft.launcher.hooks.GameModeHook;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.FlatLaf;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.converter.ActionOnLaunchConverter;
import ru.turikhay.tlauncher.ui.converter.DirectionConverter;
import ru.turikhay.tlauncher.ui.converter.LoggerTypeConverter;
import ru.turikhay.tlauncher.ui.converter.SeparateDirsConverter;
import ru.turikhay.tlauncher.ui.editor.*;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.explorer.ImageFileExplorer;
import ru.turikhay.tlauncher.ui.explorer.MediaFileExplorer;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.login.LoginException;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.support.ContributorsAlert;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SettingsPanel extends TabbedEditorPanel implements LoginForm.LoginProcessListener, LocalizableComponent {
    private static final Logger LOGGER = LogManager.getLogger(SettingsPanel.class);
    final DefaultScene scene;
    private final TabbedEditorPanel.EditorPanelTab minecraftTab;
    public final EditorFieldHandler directory;
    public final EditorFieldHandler useSeparateDir;
    public final EditorFieldHandler resolution;
    public final EditorFieldHandler fullscreen;
    public final EditorFieldHandler jre;
    public final EditorFieldHandler memory;
    public final EditorFieldHandler gpu;
    public final EditorGroupHandler versionHandler;
    public final EditorFieldHandler oldVersionsHandler; // temp
    public final EditorGroupHandler extraHandler;
    public final EditorFieldHandler launcherResolution;
    public final EditorFieldHandler laf;
    public final EditorFieldHandler font;
    public final EditorFieldHandler background;
    public final EditorFieldHandler loginFormDirection;
    public final EditorFieldHandler logger;
    public final EditorFieldHandler crashManager;
    public final EditorFieldHandler fullCommand;
    public final EditorFieldHandler launchAction;
    public final EditorGroupHandler alertUpdates;
    public final EditorFieldHandler allowNoticeDisable;
    public final EditorFieldHandler switchToBeta;
    public final EditorFieldHandler locale;
    public final HTMLPage about;
    private final BorderPanel buttonPanel;
    private final JPopupMenu popup;
    private final LocalizableMenuItem infoItem;
    private final LocalizableMenuItem defaultItem;
    private EditorHandler selectedHandler;

    public boolean ready = false;
    private boolean hideUponSave = true;

    public SettingsPanel(DefaultScene sc) {
        super(tipTheme, new Insets(5, 10, 10, 10));

        container.setNorth(null);
        setMagnifyGaps(false);

        if (tabPane.getExtendedUI() != null) {
            tabPane.getExtendedUI().setTheme(settingsTheme);
        }

        scene = sc;
        FocusListener warning = new FocusListener() {
            public void focusGained(FocusEvent e) {
                setError("settings.warning");
            }

            public void focusLost(FocusEvent e) {
                setMessage(null);
            }
        };
        FocusListener restart = new FocusListener() {
            public void focusGained(FocusEvent e) {
                setError("settings.restart");
            }

            public void focusLost(FocusEvent e) {
                setMessage(null);
            }
        };
        minecraftTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.minecraft");

        FileExplorer dirExplorer;
        try {
            dirExplorer = FileExplorer.newExplorer();
            dirExplorer.setFileSelectionMode(1);
            dirExplorer.setFileHidingEnabled(false);
        } catch (Exception e) {
            dirExplorer = null;
        }

        directory = new EditorFieldHandler("minecraft.gamedir", new EditorFileField("settings.client.gamedir.prompt", dirExplorer, false, false), warning);
        directory.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (ready) {
                    try {
                        tlauncher.getManager().getComponent(VersionLists.class).updateLocal();
                    } catch (IOException var4) {
                        Alert.showLocError("settings.client.gamedir.noaccess", var4);
                        return;
                    }

                    tlauncher.getVersionManager().asyncRefresh();
                    tlauncher.getProfileManager().recreate();
                }
            }
        });
        useSeparateDir = new EditorFieldHandler("minecraft.gamedir.separate", new EditorComboBox<>(new SeparateDirsConverter(true), Configuration.SeparateDirs.values()));
        minecraftTab.add(new EditorPair("settings.client.gamedir.label", directory, useSeparateDir));
        resolution = new EditorFieldHandler("minecraft.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", global.getDefaultClientWindowSize(), false));
        fullscreen = new EditorFieldHandler("minecraft.fullscreen", new EditorCheckBox("settings.client.resolution.fullscreen"));
        minecraftTab.add(new EditorPair("settings.client.resolution.label", resolution, fullscreen));
        minecraftTab.nextPane();
        List<ReleaseType> releaseTypes = ReleaseType.getDefinable();
        List<EditorHandler> versions = new ArrayList<>(releaseTypes.size());

        versions.add(new EditorFieldHandler("minecraft.versions.sub." + ReleaseType.SubType.REMOTE, new EditorCheckBox("settings.versions.sub." + ReleaseType.SubType.REMOTE)));
        versions.add(EditorPair.NEXT_COLUMN);

        for (int i = 0; i < releaseTypes.size(); i++) {
            ReleaseType imgExplorer = releaseTypes.get(i);
            versions.add(new EditorFieldHandler("minecraft.versions." + imgExplorer, new EditorCheckBox("settings.versions." + imgExplorer)));
            if (i % 2 == 1) {
                versions.add(EditorPair.NEXT_COLUMN);
            }
        }

        versions.add(oldVersionsHandler = new EditorFieldHandler("minecraft.versions.sub." + ReleaseType.SubType.OLD_RELEASE, new EditorCheckBox("settings.versions.sub." + ReleaseType.SubType.OLD_RELEASE)));
        versions.add(new EditorFieldHandler("minecraft.versions.only-installed", new EditorCheckBox("settings.versions.only-installed")));

        versionHandler = new EditorGroupHandler(versions);
        versionHandler.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldvalue, String newvalue) {
                TLauncher.getInstance().getVersionManager().updateVersionList();
            }
        });
        minecraftTab.add(new EditorPair("settings.versions.label", versions));
        minecraftTab.nextPane();
        jre = new EditorFieldHandler(JavaManagerConfig.PATH_JRE_TYPE, new JREComboBox(this));
        minecraftTab.add(new EditorPair("settings.jre.type.label", jre));
        final MemorySlider memorySlider = new MemorySlider(sc.loginForm.tlauncher.getMemoryAllocationService());

        minecraftTab.nextPane();
        memory = new EditorFieldHandler("minecraft.xmx", memorySlider, warning);
        minecraftTab.add(new EditorPair("settings.java.memory.label", memory));

        if (!tlauncher.getGpuManager().isEmpty()) {
            minecraftTab.nextPane();
            gpu = new EditorFieldHandler("minecraft.gpu", new GPUComboBox(this));
            minecraftTab.add(new EditorPair("settings.gpu.label", gpu));
        } else {
            gpu = null;
        }

        minecraftTab.nextPane();

        List<EditorHandler> extraHandlerList = new ArrayList<>();
        extraHandlerList.add(new EditorFieldHandler("ely.globally", new EditorCheckBox("settings.ely", true)));
        extraHandlerList.add(EditorPair.NEXT_COLUMN);
        extraHandlerList.add(new EditorFieldHandler("minecraft.servers.promoted.ingame", new EditorCheckBox("settings.promotion.ingame", true)));
        if (OS.LINUX.isCurrent()) {
            extraHandlerList.add(EditorPair.NEXT_COLUMN);
            extraHandlerList.add(new EditorFieldHandler("minecraft.gamemode", new EditorCheckBox("settings.gamemode", true)));
        }
        extraHandler = new EditorGroupHandler(extraHandlerList);
        minecraftTab.add(new EditorPair("settings.extra.label", extraHandlerList));
        minecraftTab.nextPane();

        add(minecraftTab);
        EditorPanelTab tlauncherTab = new EditorPanelTab("settings.tab.tlauncher");
        launcherResolution = new EditorFieldHandler("gui.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", global.getDefaultLauncherWindowSize(), false));
        launcherResolution.addListener(new EditorFieldListener() {
            protected void onChange(EditorHandler handler, String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    IntegerArray arr = IntegerArray.parseIntegerArray(newValue);
                    tlauncher.getFrame().setSize(arr.get(0), arr.get(1));
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.clientres.label", launcherResolution));
        font = new EditorFieldHandler("gui.font", new SettingsFontSlider(), restart);
        tlauncherTab.add(new EditorPair("settings.fontsize.label", font));
        loginFormDirection = new EditorFieldHandler("gui.direction.loginform", new EditorComboBox<>(new DirectionConverter(), Direction.values()));
        loginFormDirection.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    tlauncher.getFrame().mp.defaultScene.updateDirection();
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.direction.label", loginFormDirection));
        laf = new EditorFieldHandler(FlatLafConfiguration.KEY_STATE, new EditorComboBox<>(
                new LocalizableStringConverter<String>("settings.laf.state") {
                    @Override
                    protected String toPath(String var1) {
                        return var1;
                    }

                    @Override
                    public String fromString(String var1) {
                        return var1;
                    }

                    @Override
                    public String toValue(String var1) {
                        return var1;
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }
                },
                getLafStates()
        ));
        laf.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready && newValue != null) {
                    Alert.showLocWarning("", "settings.laf.restart", null);
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.laf.label", laf));

        boolean mediaFxAvailable = sc.getMainPane().background.getMediaFxBackground() != null;
        FileExplorer backgroundExplorer = null;
        try {
            backgroundExplorer = mediaFxAvailable ? MediaFileExplorer.newExplorer() : ImageFileExplorer.newExplorer();
        } catch (Exception e) {
            LOGGER.warn("Could not load FileExplorer", e);
        }

        background = new EditorFieldHandler("gui.background", new EditorFileField("settings.slide.list.prompt." + (mediaFxAvailable ? "media" : "image"), backgroundExplorer, true, true));
        background.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    tlauncher.getFrame().mp.background.loadBackground();
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.slide.list.label", background));
        tlauncherTab.nextPane();
        logger = new EditorFieldHandler("gui.logger", new EditorComboBox<>(new LoggerTypeConverter(), Configuration.LoggerType.values()));
        logger.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldvalue, String newvalue) {
                if (newvalue != null) {
                    tlauncher.reloadLoggerUI();
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.logger.label", logger));
        fullCommand = new EditorFieldHandler("gui.logger.fullcommand", new EditorCheckBox("settings.logger.fullcommand"));
        tlauncherTab.add(new EditorPair("settings.logger.fullcommand.label", fullCommand));
        //tlauncherTab.nextPane();
        launchAction = new EditorFieldHandler("minecraft.onlaunch", new EditorComboBox<>(new ActionOnLaunchConverter(), Configuration.ActionOnLaunch.values()));
        tlauncherTab.add(new EditorPair("settings.launch-action.label", launchAction));
        crashManager = new EditorFieldHandler("minecraft.crash", new EditorCheckBox("settings.crash.enable"));
        tlauncherTab.add(new EditorPair("settings.crash.label", crashManager));
        tlauncherTab.nextPane();

        List<EditorHandler> defReleaseTypeHandlers = new ArrayList<>();
        for (ReleaseType releaseType : new ReleaseType[]{ReleaseType.RELEASE, ReleaseType.SNAPSHOT, ReleaseType.MODIFIED}) {
            defReleaseTypeHandlers.add(new EditorFieldHandler("gui.alerton." + releaseType, new EditorCheckBox("settings.alert-on." + releaseType)));
            defReleaseTypeHandlers.add(EditorPair.NEXT_COLUMN);
        }

        alertUpdates = new EditorGroupHandler(defReleaseTypeHandlers);
        tlauncherTab.add(new EditorPair("settings.alert-on.label", defReleaseTypeHandlers));
        tlauncherTab.nextPane();

        allowNoticeDisable = new EditorFieldHandler("notice.enabled", new EditorCheckBox("notice.enable"));
        allowNoticeDisable.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    Stats.noticeStatusUpdated(Boolean.parseBoolean(newValue));
                    tlauncher.getFrame().getNotices().selectRandom();
                    Alert.showLocMessage("notice.enable.alert." + newValue);
                }
            }
        });
        tlauncherTab.add(new EditorPair("notice.enable.label", allowNoticeDisable));
        tlauncherTab.nextPane();

        switchToBeta = new EditorFieldHandler("bootstrap.switchToBeta", new EditorCheckBox("settings.switch-to-beta"));
        switchToBeta.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    Alert.showMessage("", Localizable.get("settings.restart"));
                }
            }
        });

        Optional<Boolean> canSwitchToBetaBranch = TLauncher.getInstance().getCapability("can_switch_to_beta_branch", Boolean.class);
        if (canSwitchToBetaBranch.isPresent()) {
            // only show if bootstrap supports it
            tlauncherTab.add(new EditorPair("settings.switch-to-beta.label", switchToBeta));
            tlauncherTab.nextPane();
            if (!canSwitchToBetaBranch.filter(v -> v == Boolean.TRUE).isPresent()) {
                // disable if can't switch to beta branch
                Blocker.block(switchToBeta, "cant_switch_to_beta_branch");
                switchToBeta.setPath(null); // -> ignored, not updated or saved
                switchToBeta.setValue(true);
            }
        }

        locale = new EditorFieldHandler("locale", new SettingsLocaleComboBox(this));
        locale.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldvalue, String newvalue) {
                if (SettingsPanel.this.ready) {
                    if (tlauncher.getFrame() != null) {
                        tlauncher.getFrame().updateLocales();
                    }
                    ContributorsAlert.showAlert();
                    hideUponSave = false;
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.lang.label", locale));
        add(tlauncherTab);
        EditorPanelTab aboutTab = new EditorPanelTab("settings.tab.about");
        aboutTab.setSavingEnabled(false);
        about = new HTMLPage("about.html");
        aboutTab.add(about);
        add(aboutTab);
        LocalizableButton saveButton = new LocalizableButton("settings.save");
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
        saveButton.addActionListener(e -> {
            if (!saveValues()) {
                return;
            }
            if (hideUponSave) {
                scene.setSidePanel(null);
            } else {
                hideUponSave = true;
            }
        });
        LocalizableButton defaultButton = new LocalizableButton("settings.default");
        defaultButton.addActionListener(e -> {
            if (Alert.showLocQuestion("settings.default.warning")) {
                resetValues();
            }

        });
        ExtendedButton homeButton = new ExtendedButton();
        homeButton.setIcon(Images.getIcon24("home"));
        homeButton.addActionListener(e -> {
            updateValues();
            scene.setSidePanel(null);
        });
        Dimension size1 = homeButton.getPreferredSize();
        if (size1 != null) {
            homeButton.setPreferredSize(new Dimension(SwingUtil.magnify(40), size1.height));
        }

        buttonPanel = new BorderPanel();
        buttonPanel.setCenter(sepPan(saveButton, defaultButton));
        buttonPanel.setEast(uSepPan(homeButton));
        tabPane.addChangeListener(new ChangeListener() {
            private final String aboutBlock = "abouttab";

            public void stateChanged(ChangeEvent e) {
                if (tabPane.getSelectedComponent() instanceof TabbedEditorPanel.EditorScrollPane && !((TabbedEditorPanel.EditorScrollPane) tabPane.getSelectedComponent()).getTab().getSavingEnabled()) {
                    Blocker.blockComponents("abouttab", buttonPanel);
                } else {
                    Blocker.unblockComponents("abouttab", buttonPanel);
                }

            }
        });
        container.setSouth(buttonPanel);
        popup = new JPopupMenu();
        infoItem = new LocalizableMenuItem("settings.popup.info");
        infoItem.setEnabled(false);
        popup.add(infoItem);
        defaultItem = new LocalizableMenuItem("settings.popup.default");
        defaultItem.addActionListener(e -> {
            if (selectedHandler != null) {
                resetValue(selectedHandler);
            }
        });
        popup.add(defaultItem);

        for (EditorHandler handler : handlers) {
            JComponent handlerComponent = handler.getComponent();
            if (handlerComponent != null)
                handlerComponent.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == 3) {
                            callPopup(e, handler);
                        }
                    }
                });
        }

        updateValues();
        updateLocale();
    }

    private String[] getLafStates() {
        if (!FlatLaf.getStates().isEmpty()) {
            return FlatLaf.getStates().toArray(new String[0]);
        }
        String existingValue = tlauncher.getSettings().get(FlatLafConfiguration.KEY_STATE);
        if (existingValue != null) {
            return new String[]{ existingValue };
        }
        return new String[]{ null };
    }

    public void updateValues() {
        boolean globalUnSaveable = !global.isSaveable();
        Iterator<EditorHandler> iterator = handlers.iterator();

        if (!tlauncher.isNoticeDisablingAllowed()) {
            allowNoticeDisable.getComponent().setEnabled(false);
            //allowNoticeDisableHint.getComponent().setEnabled(false);
        }

        while (true) {
            EditorHandler handler;
            String path;
            do {
                if (!iterator.hasNext()) {
                    return;
                }

                handler = iterator.next();
                path = handler.getPath();
                if (path == null)
                    continue;
                String value = global.get(path);
                handler.updateValue(value);
                setValid(handler, true);
            } while (!globalUnSaveable && global.isSaveable(path));

            Blocker.block(handler, "unsaveable");
        }
    }

    public boolean saveValues() {
        if (!checkValues()) {
            return false;
        } else {

            for (EditorHandler handler : handlers) {
                String path = handler.getPath();
                if (path == null)
                    continue;
                String value = handler.getValue();
                global.set(path, value, false);
                handler.onChange(value);
            }

            global.store();
            updateValues();
            return true;
        }
    }

    void resetValues() {

        for (EditorHandler handler : handlers) {
            resetValue(handler);
        }

    }

    void resetValue(EditorHandler handler) {
        String path = handler.getPath();
        if (global.isSaveable(path)) {
            String value = global.getDefault(path);
            handler.setValue(value);
        }
    }

    boolean canReset(EditorHandler handler) {
        String key = handler.getPath();
        return global.isSaveable(key) && global.getDefault(handler.getPath()) != null;
    }

    void callPopup(MouseEvent e, EditorHandler handler) {
        if (popup.isShowing()) {
            popup.setVisible(false);
        }

        defocus();
        int x = e.getX();
        int y = e.getY();
        selectedHandler = handler;
        updateResetMenu();
        infoItem.setVariables(handler.getPath());
        popup.show((JComponent) e.getSource(), x, y);
    }

    public void block(Object reason) {
        Blocker.blockComponents(minecraftTab, reason);
        updateResetMenu();
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(minecraftTab, reason);
        updateResetMenu();
    }

    private void updateResetMenu() {
        if (selectedHandler != null) {
            defaultItem.setEnabled(!Blocker.isBlocked(selectedHandler));
        }

    }

    public void logginingIn() throws LoginException {
        boolean ok;
        if (!tlauncher.getSettings().isFirstRun() && anyChanges()) {
            ok = askToSaveChanges();
        } else {
            ok = checkValues();
        }
        if (!ok) {
            scene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
            throw new LoginException("Invalid settings!");
        }
    }

    public void loginFailed() {
    }

    public void loginSucceed() {
    }

    public void updateLocale() {
        /*if (tlauncher.getSettings().isLikelyRussianSpeakingLocale()) {
            add(serverTab);
        } else {
            remove(serverTab);
        }*/
    }

    private boolean anyChanges() {
        for (EditorHandler handler : handlers) {
            String path = handler.getPath();
            if (path == null) {
                continue;
            }
            String value = handler.getValue();
            String existingValue = global.get(path);
            if (!Objects.equals(value, existingValue)) {
                LOGGER.debug("Found changes: {}", path);
                return true;
            }
        }
        return false;
    }

    private boolean askToSaveChanges() {
        if (Alert.showQuestion(
                "",
                Localizable.get("settings.changed.confirm")
        )) {
            if (!saveValues()) {
                return false;
            }
        } else {
            updateValues();
        }
        scene.setSidePanel(null);
        return true;
    }
}
