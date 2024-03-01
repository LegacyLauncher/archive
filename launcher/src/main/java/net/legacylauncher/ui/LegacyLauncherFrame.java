package net.legacylauncher.ui;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.configuration.LangConfiguration;
import net.legacylauncher.configuration.SimpleConfiguration;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.frames.FeedbackFrame;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.notice.NoticeManager;
import net.legacylauncher.ui.swing.Dragger;
import net.legacylauncher.ui.swing.extended.ExtendedComponentAdapter;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.util.IntegerArray;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.ExtendedThread;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LegacyLauncherFrame extends JFrame {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LegacyLauncherFrame.class);

    public static final Dimension minSize = new Dimension(700, 600);
    public static final Dimension maxSize = new Dimension(1920, 1080);
    public static final float minFontSize = 12, maxFontSize = 18;
    private static float fontSize = 12f;
    public static double magnifyDimensions = 1.;
    private final LegacyLauncherFrame instance = this;
    private final LegacyLauncher legacyLauncher;
    private final Configuration settings;
    private final LangConfiguration lang;
    private final int[] windowSize;
    private final Point maxPoint;
    public final MainPane mp;
    private String brand;
    private SimpleConfiguration proofr, uiConfig;
    private final NoticeManager notices;

    public static float getFontSize() {
        return fontSize;
    }

    public static void setFontSize(float size) {
        fontSize = size;
        magnifyDimensions = (fontSize * 16.f / 12.f) / 16.f;
    }

    public LegacyLauncherFrame(LegacyLauncher t) {
        legacyLauncher = t;
        settings = t.getSettings();
        lang = t.getLang();
        windowSize = settings.getLauncherWindowSize();
        maxPoint = new Point();
        SwingUtil.initFontSize((int) getFontSize());
        SwingUtil.setFavicons(this);
        setupUI();
        updateUILocale();
        setWindowSize();
        setWindowTitle();
        /*addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                instance.setVisible(false);
                TLauncher.kill();
            }
        });*/
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
            public void onComponentResized(ComponentEvent e) {
                updateMaxPoint();
                Dragger.update();
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
        LOGGER.trace("Packing main frame...");
        pack();
        LOGGER.trace("Resizing main pane...");
        mp.onResize();
        mp.background.loadBackground();
        updateMaxPoint();
        Dragger.ready(settings, maxPoint);
        if (LegacyLauncher.getInstance().isDebug()) {
            new LegacyLauncherFrame.TitleUpdaterThread();
        } else {
            setWindowTitle();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                setVisible(true);
            } catch (RuntimeException rE) {
                LOGGER.warn("Hidden exception on setVisible(true)", rE);
            }
            int windowState = getExtendedStateFor(settings.getInteger("gui.window"));
            if (windowState == 0) {
                setLocationRelativeTo(null);
            } else {
                setExtendedState(windowState);
            }
        });


        /*if(settings.getInteger("gui.features") < NewFeaturesFrame.INCREMENTAL) {
            final NewFeaturesFrame newFeaturesFrame = new NewFeaturesFrame(this);
            newFeaturesFrame.showAtCenter();
            newFeaturesFrame.setAlwaysOnTop(true);

            AsyncThread.execute(new Runnable() {
                @Override
                public void run() {
                    U.sleepFor(3000);
                    newFeaturesFrame.setAlwaysOnTop(false);
                }
            });
        }*/
    }

    public LegacyLauncher getLauncher() {
        return legacyLauncher;
    }

    public NoticeManager getNotices() {
        return notices;
    }

    public Point getMaxPoint() {
        return maxPoint;
    }

    public Configuration getConfiguration() {
        return settings;
    }

    public void updateLocales() {
        try {
            legacyLauncher.reloadLocale();
        } catch (Exception var2) {
            LOGGER.warn("Cannot reload settings", var2);
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

        brandBuilder.append("[").append(LegacyLauncher.getBrand()).append("]");

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

    private void updateMaxPoint() {
        maxPoint.x = getWidth();
        maxPoint.y = getHeight();
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

    public static URL getRes(String uri) {
        return LegacyLauncherFrame.class.getResource(uri);
    }

    private class TitleUpdaterThread extends ExtendedThread {
        TitleUpdaterThread() {
            super("TitleUpdater");
            updateTitle();
            start();
        }

        public void run() {
            while (isDisplayable()) {
                U.sleepFor(100L);
                setWindowTitle();
            }
            interrupt();
        }
    }
}
