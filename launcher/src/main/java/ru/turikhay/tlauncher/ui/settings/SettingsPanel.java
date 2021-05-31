package ru.turikhay.tlauncher.ui.settings;

import net.minecraft.launcher.versions.ReleaseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.managers.VersionLists;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.converter.ActionOnLaunchConverter;
import ru.turikhay.tlauncher.ui.converter.DirectionConverter;
import ru.turikhay.tlauncher.ui.converter.LoggerTypeConverter;
import ru.turikhay.tlauncher.ui.editor.*;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.explorer.ImageFileExplorer;
import ru.turikhay.tlauncher.ui.explorer.MediaFileExplorer;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginException;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.support.ContributorsAlert;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    public final EditorGroupHandler versionHandler;
    public final EditorFieldHandler oldVersionsHandler; // temp
    public final EditorGroupHandler extraHandler;
    private final TabbedEditorPanel.EditorPanelTab tlauncherTab;
    public final EditorFieldHandler launcherResolution;
    public final EditorFieldHandler systemTheme;
    public final EditorFieldHandler font;
    public final EditorFieldHandler background;
    public final EditorFieldHandler loginFormDirection;
    public final EditorFieldHandler logger;
    public final EditorFieldHandler crashManager;
    public final EditorFieldHandler fullCommand;
    public final EditorFieldHandler launchAction;
    public final EditorGroupHandler alertUpdates;
    public final EditorFieldHandler sslCheck;
    public final EditorFieldHandler allowNoticeDisable;
    public final EditorFieldHandler locale;
    private final TabbedEditorPanel.EditorPanelTab aboutTab;
    public final HTMLPage about;
    private final BorderPanel buttonPanel;
    private final LocalizableButton saveButton;
    private final LocalizableButton defaultButton;
    private final ExtendedButton homeButton;
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
        useSeparateDir = new EditorFieldHandler("minecraft.gamedir.separate", new EditorCheckBox("settings.client.gamedir.separate"));
        minecraftTab.add(new EditorPair("settings.client.gamedir.label", new EditorHandler[]{directory, useSeparateDir}));
        resolution = new EditorFieldHandler("minecraft.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", global.getDefaultClientWindowSize(), false));
        fullscreen = new EditorFieldHandler("minecraft.fullscreen", new EditorCheckBox("settings.client.resolution.fullscreen"));
        minecraftTab.add(new EditorPair("settings.client.resolution.label", new EditorHandler[]{resolution, fullscreen}));
        minecraftTab.nextPane();
        List releaseTypes = ReleaseType.getDefinable();
        ArrayList versions = new ArrayList(releaseTypes.size());
        Iterator size = ReleaseType.getDefinable().iterator();

        versions.add(new EditorFieldHandler("minecraft.versions.sub." + ReleaseType.SubType.REMOTE, new EditorCheckBox("settings.versions.sub." + ReleaseType.SubType.REMOTE)));
        versions.add(EditorPair.NEXT_COLUMN);

        int i = 0;
        while (size.hasNext()) {
            ReleaseType imgExplorer = (ReleaseType) size.next();
            versions.add(new EditorFieldHandler("minecraft.versions." + imgExplorer, new EditorCheckBox("settings.versions." + imgExplorer)));
            if (++i % 2 == 0)
                versions.add(EditorPair.NEXT_COLUMN);
        }

        versions.add(oldVersionsHandler = new EditorFieldHandler("minecraft.versions.sub." + ReleaseType.SubType.OLD_RELEASE, new EditorCheckBox("settings.versions.sub." + ReleaseType.SubType.OLD_RELEASE)));
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
        final SettingsMemorySlider memorySlider = new SettingsMemorySlider(this);

        minecraftTab.nextPane();
        memory = new EditorFieldHandler("minecraft.memory", memorySlider, warning);
        minecraftTab.add(new EditorPair("settings.java.memory.label", new EditorHandler[]{memory}));

        minecraftTab.nextPane();

        List<EditorHandler> extraHandlerList = new ArrayList<>();
        extraHandlerList.add(new EditorFieldHandler("ely.globally", new EditorCheckBox("settings.ely", true)));
        extraHandlerList.add(EditorPair.NEXT_COLUMN);
        extraHandlerList.add(new EditorFieldHandler("minecraft.servers.promoted.ingame", new EditorCheckBox("settings.promotion.ingame", true)));
        extraHandler = new EditorGroupHandler(extraHandlerList);
        minecraftTab.add(new EditorPair("settings.extra.label", extraHandlerList));
        minecraftTab.nextPane();

        add(minecraftTab);
        tlauncherTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.tlauncher");
        launcherResolution = new EditorFieldHandler("gui.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", global.getDefaultLauncherWindowSize(), false));
        launcherResolution.addListener(new EditorFieldListener() {
            protected void onChange(EditorHandler handler, String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    IntegerArray arr = IntegerArray.parseIntegerArray(newValue);
                    tlauncher.getFrame().setSize(arr.get(0), arr.get(1));
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.clientres.label", new EditorHandler[]{launcherResolution}));
        font = new EditorFieldHandler("gui.font", new SettingsFontSlider(), restart);
        tlauncherTab.add(new EditorPair("settings.fontsize.label", font));
        loginFormDirection = new EditorFieldHandler("gui.direction.loginform", new EditorComboBox(new DirectionConverter(), Direction.values()));
        loginFormDirection.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    tlauncher.getFrame().mp.defaultScene.updateDirection();
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.direction.label", new EditorHandler[]{loginFormDirection}));
        systemTheme = new EditorFieldHandler("gui.systemlookandfeel", new EditorCheckBox("settings.systemlnf"));
        systemTheme.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    Alert.showLocWarning("settings.systemlnf.note.title", "settings.systemlnf.note." + newValue, null);
                }

            }
        });
        tlauncherTab.add(new EditorPair("settings.systemlnf.label", new EditorHandler[]{systemTheme}));
        //tlauncherTab.nextPane();
        //tlauncherTab.nextPane();

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
        tlauncherTab.add(new EditorPair("settings.slide.list.label", new EditorHandler[]{background}));
        tlauncherTab.nextPane();
        logger = new EditorFieldHandler("gui.logger", new EditorComboBox(new LoggerTypeConverter(), Configuration.LoggerType.values()));
        logger.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldvalue, String newvalue) {
                if (newvalue != null) {
                    tlauncher.reloadLoggerUI();
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.logger.label", new EditorHandler[]{logger}));
        fullCommand = new EditorFieldHandler("gui.logger.fullcommand", new EditorCheckBox("settings.logger.fullcommand"));
        tlauncherTab.add(new EditorPair("settings.logger.fullcommand.label", new EditorHandler[]{fullCommand}));
        //tlauncherTab.nextPane();
        launchAction = new EditorFieldHandler("minecraft.onlaunch", new EditorComboBox(new ActionOnLaunchConverter(), Configuration.ActionOnLaunch.values()));
        tlauncherTab.add(new EditorPair("settings.launch-action.label", new EditorHandler[]{launchAction}));
        crashManager = new EditorFieldHandler("minecraft.crash", new EditorCheckBox("settings.crash.enable"));
        tlauncherTab.add(new EditorPair("settings.crash.label", crashManager));
        tlauncherTab.nextPane();

        List<EditorHandler> defReleaseTypeHandlers = new ArrayList<EditorHandler>();
        for (ReleaseType releaseType : new ReleaseType[]{ReleaseType.RELEASE, ReleaseType.SNAPSHOT, ReleaseType.MODIFIED}) {
            defReleaseTypeHandlers.add(new EditorFieldHandler("gui.alerton." + releaseType, new EditorCheckBox("settings.alert-on." + releaseType)));
            defReleaseTypeHandlers.add(EditorPair.NEXT_COLUMN);
        }

        alertUpdates = new EditorGroupHandler(defReleaseTypeHandlers);
        tlauncherTab.add(new EditorPair("settings.alert-on.label", defReleaseTypeHandlers));
        tlauncherTab.nextPane();

        sslCheck = new EditorFieldHandler("connection.ssl", new EditorCheckBox("settings.ssl"));
        sslCheck.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    Alert.showLocWarning("settings.ssl.warning.title", "settings.ssl.warning.value." + newValue, null);
                }
            }
        });
        tlauncherTab.add(new EditorPair("settings.ssl.label", sslCheck));
        tlauncherTab.nextPane();

        allowNoticeDisable = new EditorFieldHandler("notice.enabled", new EditorCheckBox("notice.enable"));
        allowNoticeDisable.addListener(new EditorFieldChangeListener() {
            protected void onChange(String oldValue, String newValue) {
                if (SettingsPanel.this.ready) {
                    Stats.noticeStatusUpdated(Boolean.valueOf(newValue));
                    tlauncher.getFrame().getNotices().selectRandom();
                    Alert.showLocMessage("notice.enable.alert." + newValue);
                }
            }
        });
        tlauncherTab.add(new EditorPair("notice.enable.label", allowNoticeDisable));
        tlauncherTab.nextPane();

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
        tlauncherTab.add(new EditorPair("settings.lang.label", new EditorHandler[]{locale}));
        add(tlauncherTab);
        aboutTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.about");
        aboutTab.setSavingEnabled(false);
        about = new HTMLPage("about.html");
        aboutTab.add(about);
        add(aboutTab);
        saveButton = new LocalizableButton("settings.save");
        saveButton.setFont(saveButton.getFont().deriveFont(1));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!saveValues()) {
                    return;
                }
                if (hideUponSave) {
                    scene.setSidePanel(null);
                } else {
                    hideUponSave = true;
                }
            }
        });
        defaultButton = new LocalizableButton("settings.default");
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Alert.showLocQuestion("settings.default.warning")) {
                    resetValues();
                }

            }
        });
        homeButton = new ExtendedButton();
        homeButton.setIcon(Images.getIcon("home.png", SwingUtil.magnify(20)));
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateValues();
                scene.setSidePanel(null);
            }
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
                    Blocker.blockComponents("abouttab", (Component[]) (new Component[]{buttonPanel}));
                } else {
                    Blocker.unblockComponents("abouttab", (Component[]) (new Component[]{buttonPanel}));
                }

            }
        });
        container.setSouth(buttonPanel);
        popup = new JPopupMenu();
        infoItem = new LocalizableMenuItem("settings.popup.info");
        infoItem.setEnabled(false);
        popup.add(infoItem);
        defaultItem = new LocalizableMenuItem("settings.popup.default");
        defaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedHandler != null) {
                    resetValue(selectedHandler);
                }
            }
        });
        popup.add(defaultItem);
        Iterator var10 = handlers.iterator();

        while (var10.hasNext()) {
            final EditorHandler handler = (EditorHandler) var10.next();
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

    public void updateValues() {
        boolean globalUnSaveable = !global.isSaveable();
        Iterator var3 = handlers.iterator();

        if(!tlauncher.isNoticeDisablingAllowed()) {
            allowNoticeDisable.getComponent().setEnabled(false);
            //allowNoticeDisableHint.getComponent().setEnabled(false);
        }

        while (true) {
            EditorHandler handler;
            String path;
            do {
                if (!var3.hasNext()) {
                    return;
                }

                handler = (EditorHandler) var3.next();
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
            Iterator var2 = handlers.iterator();

            while (var2.hasNext()) {
                EditorHandler handler = (EditorHandler) var2.next();
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
        Iterator var2 = handlers.iterator();

        while (var2.hasNext()) {
            EditorHandler handler = (EditorHandler) var2.next();
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
        if (!checkValues()) {
            scene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
            throw new LoginException("Invalid settings!");
        }
    }

    public void loginFailed() {
    }

    public void loginSucceed() {
    }

    public void updateLocale() {
        /*if (tlauncher.getSettings().isUSSRLocale()) {
            add(serverTab);
        } else {
            remove(serverTab);
        }*/
    }
}
