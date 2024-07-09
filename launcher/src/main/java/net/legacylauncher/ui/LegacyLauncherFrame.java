package net.legacylauncher.ui;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.BuildConfig;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.configuration.LangConfiguration;
import net.legacylauncher.configuration.SimpleConfiguration;
import net.legacylauncher.portals.Portals;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.frames.FeedbackFrame;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.notice.NoticeManager;
import net.legacylauncher.ui.swing.extended.ExtendedComponentAdapter;
import net.legacylauncher.ui.theme.SystemTheme;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.util.IntegerArray;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.shared.FlatLafConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LegacyLauncherFrame extends JFrame {
    public static final Dimension minSize = new Dimension(700, 600);
    public static final Dimension maxSize = new Dimension(1920, 1080);
    public static final float minFontSize = 12, maxFontSize = 18;
    @Getter
    private static float fontSize = 12f;
    public static double magnifyDimensions = 1.;
    private final LegacyLauncherFrame instance = this;
    private final LegacyLauncher legacyLauncher;
    private final Configuration settings;
    private final LangConfiguration lang;
    private final int[] windowSize;
    public final MainPane mp;
    private String brand;
    @Getter
    private final NoticeManager notices;

    public static void setFontSize(float size) {
        fontSize = size;
        magnifyDimensions = (fontSize * 16.f / 12.f) / 16.f;
    }

    public LegacyLauncherFrame(LegacyLauncher t) {
        legacyLauncher = t;
        settings = t.getSettings();
        lang = t.getLang();
        windowSize = settings.getLauncherWindowSize();
        SwingUtil.initFontSize((int) getFontSize());
        SwingUtil.setFavicons(this);
        setupUI();
        updateUILocale();
        setWindowSize();
        setWindowTitle();
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                mp.background.pauseBackground();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                mp.background.startBackground();
            }

            public void windowActivated(WindowEvent e) {
                mp.background.startBackground();
            }

            public void windowDeactivated(WindowEvent e) {
                mp.background.pauseBackground();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (settings.getBoolean("feedback") || legacyLauncher.getBootConfig().getFeedback() == null) {
                    instance.setVisible(false);
                    LegacyLauncher.kill();
                    return;
                }

                String url;
                if (legacyLauncher.getBootConfig().getFeedback().containsKey(legacyLauncher.getSettings().getLocale().toString())) {
                    url = legacyLauncher.getBootConfig().getFeedback().get(legacyLauncher.getSettings().getLocale().toString());
                } else if (legacyLauncher.getBootConfig().getFeedback().containsKey("global")) {
                    url = legacyLauncher.getBootConfig().getFeedback().get("global");
                } else {
                    instance.setVisible(false);
                    LegacyLauncher.kill();
                    return;
                }
                settings.set("feedback", true);
                new FeedbackFrame(LegacyLauncherFrame.this, url);
            }
        });
        addComponentListener(new ExtendedComponentAdapter(this) {
            @Override
            public void onComponentResized() {
                if (mp != null && mp.defaultScene != null) {
                    boolean lock = getExtendedState() != 0;
                    IntegerArray arr = new IntegerArray(getWidth(), getHeight());
                    if (mp.defaultScene.settingsForm.isLoaded()) {
                        Blocker.setBlocked(mp.defaultScene.settingsForm.get().launcherResolution, "extended", lock);
                        if (!lock) {
                            mp.defaultScene.settingsForm.get().launcherResolution.setValue(arr);
                        }
                    }
                    if (!lock) {
                        settings.set("gui.size", arr);
                    }
                }
            }

            @Override
            public void componentShown(ComponentEvent e) {
                instance.validate();
                instance.repaint();
                instance.toFront();
                mp.background.startBackground();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                mp.background.pauseBackground();
            }
        });
        addWindowStateListener(e -> {
            int newState = LegacyLauncherFrame.getExtendedStateFor(e.getNewState());
            if (newState != -1) {
                settings.set("gui.window", newState);
            }
        });
        notices = new NoticeManager(this, t.getBootConfig());
        mp = new MainPane(this);
        add(mp);
        log.trace("Packing main frame...");
        pack();
        log.trace("Resizing main pane...");
        mp.onResize();
        mp.background.loadBackground();
        if (LegacyLauncher.getInstance().isDebug()) {
            TitleUpdater titleUpdater = new TitleUpdater();
            titleUpdater.self = AsyncThread.DELAYER.scheduleWithFixedDelay(
                    titleUpdater, 1, 1, TimeUnit.SECONDS
            );
        } else {
            setWindowTitle();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                setVisible(true);
            } catch (RuntimeException rE) {
                log.warn("Hidden exception on setVisible(true)", rE);
            }
            int windowState = getExtendedStateFor(settings.getInteger("gui.window"));
            if (windowState == 0) {
                setLocationRelativeTo(null);
            } else {
                setExtendedState(windowState);
            }
        });

        AutoCloseable lafListener = Portals.getPortal().subscribeForColorSchemeChanges((colorScheme) -> updateLaf());
        addContainerListener(new ContainerAdapter() {
            @Override
            public void componentRemoved(ContainerEvent e) {
                try {
                    lafListener.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    public LegacyLauncher getLauncher() {
        return legacyLauncher;
    }

    public Configuration getConfiguration() {
        return settings;
    }

    public void updateLocales() {
        try {
            legacyLauncher.reloadLocale();
        } catch (Exception var2) {
            log.warn("Cannot reload settings", var2);
            return;
        }

        LocalizableMenuItem.updateLocales();
        updateUILocale();
        notices.updateLocale();
        Localizable.updateContainer(this);
        setWindowTitle();
    }

    public void updateTitle() {
        StringBuilder brandBuilder = new StringBuilder();

        if (!LegacyLauncher.getInstance().isDebug()) {
            brandBuilder.append(U.getMinorVersion(LegacyLauncher.getVersion())).append(" ");
        }

        brandBuilder.append("[").append(BuildConfig.FULL_BRAND).append("]");

        if (LegacyLauncher.getInstance().isDebug()) {
            brandBuilder.append(" [DEBUG]");
        }

        brand = brandBuilder.toString();
    }

    public void setWindowTitle() {
        updateTitle();
        String title;
        if (LegacyLauncher.getInstance().isDebug()) {
            title = String.format(java.util.Locale.ROOT, "Legacy Launcher %s [%s]", brand, U.memoryStatus());
        } else {
            title = String.format(java.util.Locale.ROOT, "Legacy Launcher %s", brand);
        }

        setTitle(title);
    }

    private void setWindowSize() {
        int width = Math.min(windowSize[0], maxSize.width);
        int height = Math.min(windowSize[1], maxSize.height);
        Dimension curSize = new Dimension(width, height);
        setMinimumSize(SwingUtil.magnify(minSize));
        setPreferredSize(curSize);
    }

    private void setupUI() {
        if (OS.WINDOWS.isCurrent()) {
            UIManager.put("FileChooser.useSystemExtensionHiding", false); // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8179014
        }

        UIManager.put("FileChooser.newFolderErrorSeparator", ": ");
        UIManager.put("FileChooser.readOnly", Boolean.FALSE);
        UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(0, 8, 6, 8));

        String themeFile = settings.get("gui.theme");
        String name = null;
        InputStream in;

        try {
            if (themeFile != null && new File(themeFile).isFile()) {
                name = themeFile;
                in = Files.newInputStream(Paths.get(themeFile));
            } else {
                name = "modern";
                in = Theme.class.getResourceAsStream("theme.properties");
            }

            Theme.loadTheme(name, in);
        } catch (Exception e) {
            Alert.showError("Could not load theme: " + name, e);
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private SimpleConfiguration uiConfig;

    private void updateUILocale() {
        if (uiConfig == null) {
            try {
                uiConfig = new SimpleConfiguration(getClass().getResource("/lang/_ui.properties"));
            } catch (Exception var4) {
                return;
            }
        }

        for (String key : uiConfig.getKeys()) {
            String value = uiConfig.get(key);
            if (value != null) {
                UIManager.put(key, lang.get(value));
            }
        }

    }

    public void setSize(int width, int height) {
        if (getWidth() != width || getHeight() != height) {
            if (getExtendedState() == 0) {
                boolean show = isVisible();
                if (show) {
                    setVisible(false);
                }

                super.setSize(width, height);
                if (show) {
                    setVisible(true);
                    setLocationRelativeTo(null);
                }

            }
        }
    }

    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    public void updateLaf() {
        FlatLafConfiguration config = FlatLafConfiguration.parseFromMap(LegacyLauncher.getInstance().getSettings());
        FlatLaf.initialize(config, true);
        SystemTheme.getSystemTheme().updateUI();
        FlatLaf.updateLafInWindows();
    }

    private static int getExtendedStateFor(int state) {
        switch (state) {
            case 0:
            case 2:
            case 4:
            case 6:
                return state;
            case 1:
            case 3:
            case 5:
            default:
                return -1;
        }
    }

    private class TitleUpdater implements Runnable {
        private ScheduledFuture<?> self;

        public void run() {
            SwingUtil.later(() -> {
                if (isDisplayable()) {
                    setWindowTitle();
                } else if (self != null) {
                    self.cancel(true);
                    self = null;
                }
            });
        }
    }
}
