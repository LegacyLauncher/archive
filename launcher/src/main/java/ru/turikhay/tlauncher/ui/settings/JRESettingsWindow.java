package ru.turikhay.tlauncher.ui.settings;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.jre.JavaRuntimeLocal;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.ui.editor.EditorFileField;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.swing.DocumentChangeListener;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.JavaVersion;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class JRESettingsWindow extends ExtendedFrame implements LocalizableComponent {
    private static final int WIDTH = SwingUtil.magnify(450), HALF_WIDTH = SwingUtil.magnify(300);

    private final JREComboBox comboBox;

    private final LocalizableRadioButton
            recommendedRadioButton, currentRadioButton, customRadioButton;

    private final ExtendedPanel pathPanel;
    private final CardLayout pathCards;

    private final EditorFileField customPathField;
    private final LocalizableLabel customPathHint;
    private final LocalizableLabel recommendedPathHint0;
    private final LocalizableTextField recommendedPathField;
    private final LocalizableHTMLLabel pathMessage;
    private final LocalizableTextField jvmArgsField;
    private final LocalizableTextField mcArgsField;
    private final LocalizableCheckbox useOptimizedArgsCheckbox;
    private final LocalizableLabel settingsSavedLabel;

    private boolean saveValues;

    public JRESettingsWindow(JREComboBox comboBox) {
        this.comboBox = comboBox;

        setResizable(false);
        setAlwaysOnTop(true);

        ExtendedPanel p = new ExtendedPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(
                SwingUtil.magnify(20),
                SwingUtil.magnify(20),
                SwingUtil.magnify(20),
                SwingUtil.magnify(20)
        ));
        setContentPane(p);

        addLabel("select", "check-circle-o");

        recommendedRadioButton = addType("recommended", WIDTH);
        recommendedRadioButton.addActionListener(e -> onRecommended());
        currentRadioButton = addType("current", WIDTH);
        currentRadioButton.addActionListener(e -> onCurrent());
        customRadioButton = addType("custom", WIDTH);
        customRadioButton.addActionListener(e -> onCustom());

        ButtonGroup group = new ButtonGroup();
        group.add(recommendedRadioButton);
        group.add(currentRadioButton);
        group.add(customRadioButton);

        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(20))));
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(20))));

        addLabel("configure", "gear");

        ExtendedPanel cfgs = new ExtendedPanel();
        cfgs.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        cfgs.setLayout(new GridBagLayout());
        c.gridx = 0;
        cfgs.add(Box.createRigidArea(new Dimension(1, 1)), c);
        c.gridx = 1;
        cfgs.add(Box.createRigidArea(new Dimension(SwingUtil.magnify(HALF_WIDTH), 1)), c);

        pathPanel = new ExtendedPanel();
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathCards = new CardLayout();
        pathPanel.setLayout(pathCards);

        LocalizableTextField currentPathField = initCurrentPath();
        customPathHint = createCustomPathHint();
        customPathField = initCustomPath();
        recommendedPathHint0 = createRecommendedPathHint();
        recommendedPathField = initRecommendedPath();
        pathMessage = initPathMessage();

        addConfig(cfgs, c, "path", pathPanel, GridBagConstraints.FIRST_LINE_START);

        jvmArgsField = createJvmArgsField();
        useOptimizedArgsCheckbox = createUseOptimizedArgsCheckbox();

        initJvmArgs(cfgs, c);
        mcArgsField = addConfig(cfgs, c, "mc-args");

        add(cfgs);

        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(20))));
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(20))));

        settingsSavedLabel = new LocalizableLabel(
                "settings.jre.window.bottom.save");
        settingsSavedLabel.setIcon(Images.getIcon16("save-1").getDisabledInstance());
        settingsSavedLabel.setIconTextGap(SwingUtil.magnify(15));
        settingsSavedLabel.setForeground(Theme.getTheme().getSemiForeground());
        settingsSavedLabel.setVisible(false);

        LocalizableButton closeButton = new LocalizableButton("settings.jre.window.bottom.close");
        closeButton.setPreferredSize(new Dimension(HALF_WIDTH / 2, SwingUtil.magnify(40)));
        closeButton.addActionListener(e -> JRESettingsWindow.this.dispose());

        BorderPanel bottomPanel = new BorderPanel();
        bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.setWest(settingsSavedLabel);
        bottomPanel.setEast(closeButton);

        add(bottomPanel);

        pack();
    }

    void showSettingsWindow() {
        updateLocale();
        updateSelfValues();
        super.showAtCenter();
    }

    void selectedVersionChanged(VersionSyncInfo versionSyncInfo) {
        if (recommendedRadioButton.isSelected()) {
            onRecommended();
        }
    }

    void javaVersionCallback() {
        String settingsValue = customPathField.getSettingsValue();
        Future<JavaVersion> f = comboBox.javaVersionCache.get(settingsValue);
        if (!f.isDone()) {
            return;
        }
        JavaVersion javaVersion;
        try {
            javaVersion = f.get();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            setCustomPathHint(true, "settings.jre.window.configure.path.custom.hint.version.error");
            return;
        }
        setCustomPathHint(false, "settings.jre.window.configure.path.custom.hint.version.detected",
                javaVersion.getVersion());
    }

    private void updateSelfValues() {
        saveValues = false;

        JavaManagerConfig.JreType jreType = JavaManagerConfig.createByType(comboBox.sp.jre.getValue());
        if (jreType instanceof JavaManagerConfig.Recommended) {
            recommendedRadioButton.doClick();
        } else if (jreType instanceof JavaManagerConfig.Current) {
            currentRadioButton.doClick();
        } else if (jreType instanceof JavaManagerConfig.Custom) {
            customRadioButton.doClick();
        } else {
            throw new RuntimeException("unknown jreType");
        }
        JavaManagerConfig javaManagerConfig = comboBox.sp.global.get(JavaManagerConfig.class);
        useOptimizedArgsCheckbox.setSelected(javaManagerConfig.useOptimizedArguments());
        jvmArgsField.setValue(javaManagerConfig.getArgs().orElse(null));
        mcArgsField.setValue(javaManagerConfig.getMinecraftArgs().orElse(null));

        saveValues = true;
    }

    private void saveSelfValues() {
        if (!saveValues) {
            return;
        }
        JavaManagerConfig javaManagerConfigOld = comboBox.sp.global.get(JavaManagerConfig.class);
        JavaManagerConfig javaManagerConfig = comboBox.sp.global.get(JavaManagerConfig.class);
        javaManagerConfig.setArgs(jvmArgsField.getValue());
        javaManagerConfig.setMcArgs(mcArgsField.getValue());
        javaManagerConfig.setUseOptimizedArguments(useOptimizedArgsCheckbox.isSelected());
        if (!javaManagerConfigOld.equals(javaManagerConfig)) {
            comboBox.sp.global.set(javaManagerConfig);
            onValuesSaved();
        }
    }

    private final AtomicInteger saveCount = new AtomicInteger();

    private void onValuesSaved() {
        final int saveCount = this.saveCount.incrementAndGet();
        AsyncThread.execute(() -> {
            SwingUtil.later(() -> settingsSavedLabel.setVisible(true));
            Thread.sleep(5000);
            SwingUtil.later(() -> {
                if (saveCount == JRESettingsWindow.this.saveCount.get()) {
                    SwingUtil.later(() -> settingsSavedLabel.setVisible(false));
                }
            });
            return null;
        });
    }

    private void onRecommended() {
        saveJreType(JavaManagerConfig.Recommended.TYPE);

        VersionSyncInfo version = comboBox.sp.scene.loginForm.versions.getVersion();
        if (version == null || !version.isInstalled()) {
            pathMessage.setText("settings.jre.window.configure.path.recommended.unknown");
            showPath("pathMessage");
            return;
        }
        CompleteVersion localCompleteVersion = version.getLocalCompleteVersion();
        CompleteVersion.JavaVersion javaVersion = localCompleteVersion.getJavaVersion();
        if (javaVersion == null) {
            javaVersion = TLauncher.getInstance().getJavaManager()
                    .getFallbackRecommendedVersion(localCompleteVersion, false);

            if (javaVersion == null) {
                pathMessage.setText("settings.jre.window.configure.path.recommended.current",
                        localCompleteVersion.getID());
                showPath("pathMessage");
                return;
            }
        }

        Optional<JavaRuntimeLocal> localRuntimeOpt = TLauncher.getInstance().getJavaManager().getDiscoverer()
                .getCurrentPlatformRuntime(javaVersion.getComponent());
        if (!localRuntimeOpt.isPresent()) {
            pathMessage.setText("settings.jre.window.configure.path.recommended.not-installed",
                    localCompleteVersion.getID(), javaVersion.getComponent());
            showPath("pathMessage");
            return;
        }

        String javaVersionId;
        try {
            javaVersionId = localRuntimeOpt.get().getVersion();
        } catch (IOException e) {
            javaVersionId = localRuntimeOpt.get().getName();
        }


        recommendedPathField.setText(localRuntimeOpt.get().getDirectory().getAbsolutePath());
        setRecommendedPathHint0(localCompleteVersion.getID(), javaVersionId);
        showPath("recommendedPath");
    }

    private void onCurrent() {
        showPath("currentPath");
        saveJreType(JavaManagerConfig.Current.TYPE);
    }

    private void onCustom() {
        JavaManagerConfig.Custom custom = comboBox.sp.global.get(JavaManagerConfig.Custom.class);
        customPathField.setSettingsValue(custom.getPath().orElse(null));
        showPath("customPath");
        saveJreType(JavaManagerConfig.Custom.TYPE);
    }

    private void saveJreType(String type) {
        if (type.equals(comboBox.sp.global.get(JavaManagerConfig.PATH_JRE_TYPE))) {
            return;
        }
        comboBox.sp.global.set(JavaManagerConfig.PATH_JRE_TYPE, type);
        comboBox.sp.updateValues();
        onValuesSaved();
    }

    private void showPath(String name) {
        pathCards.show(pathPanel, name);
    }

    private void initPathPanelCard(String name, JComponent field, JComponent hint) {
        ExtendedPanel customPath = new ExtendedPanel();
        customPath.setLayout(new BoxLayout(customPath, BoxLayout.Y_AXIS));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        customPath.add(field);
        customPath.add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(5))));
        customPath.add(hint);
        pathPanel.add(customPath, name);
    }

    private LocalizableLabel createCustomPathHint() {
        return new LocalizableLabel();
    }

    private void setCustomPathHint(boolean isError, String path, Object... vars) {
        if (isError) {
            customPathHint.setForeground(Theme.getTheme().getFailure());
        } else {
            customPathHint.setForeground(Theme.getTheme().getForeground());
        }
        customPathHint.setText(path, vars);
    }

    private EditorFileField initCustomPath() {
        FileExplorer fileExplorer;
        try {
            fileExplorer = FileExplorer.newExplorer();
        } catch (Exception e) {
            fileExplorer = null;
        }
        EditorFileField customPathField = new EditorFileField(
                "settings.jre.window.configure.path.custom.placeholder" + (OS.WINDOWS.isCurrent() ? ".windows" : ""),
                "settings.jre.window.configure.path.custom.browse",
                fileExplorer, true, false);
        customPathField.addChangeListener(s -> {
            if (s != null) {
                File file = new File(s);
                if (file.isFile()) {
                    comboBox.sp.global.set(new JavaManagerConfig.Custom(s));
                    Future<JavaVersion> future = comboBox.javaVersionCache.get(s);
                    if (future.isDone()) {
                        javaVersionCallback();
                        comboBox.sp.jre.getComponent().repaint();
                    } else {
                        setCustomPathHint(false, "settings.jre.window.configure.path.custom.hint.version.detecting");
                    }
                    return;
                }
            }
            comboBox.sp.global.set(new JavaManagerConfig.Custom(s));
            setCustomPathHint(false, "settings.jre.window.configure.path.custom.hint.enter-path");
        });
        initPathPanelCard("customPath", customPathField, customPathHint);
        return customPathField;
    }

    private LocalizableTextField initCurrentPath() {
        LocalizableTextField currentPathField = new LocalizableTextField();
        currentPathField.setText(OS.getJavaPath());
        currentPathField.setEditable(false);
        LocalizableLabel currentPathHint = new LocalizableLabel(
                "settings.jre.window.configure.path.current.hint");
        initPathPanelCard("currentPath", currentPathField, currentPathHint);
        return currentPathField;
    }

    private LocalizableLabel createRecommendedPathHint() {
        return new LocalizableLabel();
    }

    private void setRecommendedPathHint0(String version, String javaVersion) {
        recommendedPathHint0.setText("settings.jre.window.configure.path.recommended.actual.hint.0",
                version, javaVersion);
    }

    private LocalizableTextField initRecommendedPath() {
        LocalizableTextField recommendedPathField = new LocalizableTextField();
        recommendedPathField.setEditable(false);
        LocalizableButton recommendedPathButton = new LocalizableButton(
                "settings.jre.window.configure.path.recommended.actual.open");
        recommendedPathButton.addActionListener(e -> {
            String path = recommendedPathField.getValue();
            if (path != null) {
                JRESettingsWindow.this.setAlwaysOnTop(false);
                OS.openFolder(new File(path));
            }
        });
        LocalizableLabel recommendedPathHint1 = new LocalizableLabel(
                "settings.jre.window.configure.path.recommended.actual.hint.1");
        recommendedPathHint1.setForeground(Theme.getTheme().getSemiForeground());
        Map<TextAttribute, Object> attributes = new HashMap<>(recommendedPathHint1.getFont().getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        recommendedPathHint1.setFont(recommendedPathHint1.getFont().deriveFont(attributes));
        recommendedPathHint1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recommendedPathHint1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JRESettingsWindow.this.setAlwaysOnTop(false);
                OS.openLink("https://wiki.llaun.ch/" + (comboBox.sp.global.isLikelyRussianSpeakingLocale() ? "" : "en:") + "guide:override-jre");
            }
        });
        ExtendedPanel recommendedPathHint = new ExtendedPanel();
        recommendedPathHint.setInsets(new Insets(SwingUtil.magnify(5), 0, 0, 0));
        recommendedPathHint.setLayout(new BoxLayout(recommendedPathHint, BoxLayout.Y_AXIS));
        recommendedPathHint.add(recommendedPathHint0);
        recommendedPathHint.add(Box.createRigidArea(new Dimension(0, SwingUtil.magnify(2))));
        recommendedPathHint.add(recommendedPathHint1);
        BorderPanel recommendedPath = new BorderPanel();
        recommendedPath.setCenter(recommendedPathField);
        recommendedPath.setEast(recommendedPathButton);
        recommendedPath.setSouth(recommendedPathHint);
        pathPanel.add(recommendedPath, "recommendedPath");
        return recommendedPathField;
    }

    private LocalizableHTMLLabel initPathMessage() {
        LocalizableHTMLLabel pathMessage = new LocalizableHTMLLabel();
        pathMessage.setVerticalAlignment(SwingConstants.TOP);
        pathMessage.setLabelWidth(HALF_WIDTH);
        pathPanel.add(pathMessage, "pathMessage");
        return pathMessage;
    }

    private LocalizableTextField createJvmArgsField() {
        LocalizableTextField l = new LocalizableTextField(
                "settings.jre.window.configure.jvm-args.hint") {
            @Override
            protected void updateStyle() {
            }
        };
        l.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                saveSelfValues();
            }
        });
        return l;
    }

    private LocalizableCheckbox createUseOptimizedArgsCheckbox() {
        LocalizableCheckbox c = new LocalizableCheckbox(
                "settings.jre.window.configure.jvm-args.use-optimized");
        c.addActionListener(e -> saveSelfValues());
        return c;
    }

    private void initJvmArgs(ExtendedPanel cfgs, GridBagConstraints c) {
        ExtendedPanel jvmArgsPanel = new ExtendedPanel();
        jvmArgsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jvmArgsPanel.setLayout(new BoxLayout(jvmArgsPanel, BoxLayout.Y_AXIS));
        jvmArgsPanel.add(jvmArgsField);
        jvmArgsPanel.add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(10))));
        jvmArgsPanel.add(useOptimizedArgsCheckbox);
        addConfig(cfgs, c, "jvm-args", jvmArgsPanel, GridBagConstraints.FIRST_LINE_START);
    }

    private void addConfig(ExtendedPanel p, GridBagConstraints c, String path, JComponent component, int anchor) {
        LocalizableLabel label = new LocalizableLabel("settings.jre.window.configure." + path);

        c.gridy++;

        c.gridx = 0;
        c.anchor = anchor;
        c.insets = SwingUtil.magnify(new Insets(10, 0, 0, 10));
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        p.add(label, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = SwingUtil.magnify(new Insets(10, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        p.add(component, c);
    }

    private LocalizableTextField addConfig(ExtendedPanel p, GridBagConstraints c, String path) {
        LocalizableTextField textField = new LocalizableTextField("settings.jre.window.configure." + path + ".hint") {
            @Override
            protected void updateStyle() {
            }
        };
        textField.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                saveSelfValues();
            }
        });
        addConfig(p, c, path, textField, GridBagConstraints.LINE_START);
        return textField;
    }

    private LocalizableRadioButton addType(String type, int width) {
        LocalizableRadioButton b = new LocalizableRadioButton("settings.jre.window.select." + type);
        add(b);
        LocalizableHTMLLabel l = new LocalizableHTMLLabel("settings.jre.window.select." + type + ".hint");
        l.setLabelWidth(width);
        l.setForeground(Theme.getTheme().getSemiForeground());
        add(l);
        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(10))));

        return b;
    }

    private LocalizableLabel addLabel(String path, String icon) {
        LocalizableLabel l = new LocalizableLabel("settings.jre.window." + path + ".label");
        l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D() + 3.f));
        l.setIconTextGap(SwingUtil.magnify(10));
        l.setForeground(Theme.getTheme().getSemiForeground());
        l.setIcon(Images.getIcon24(icon).getDisabledInstance());
        add(l);
        add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(20))));
        return l;
    }

    @Override
    public void updateLocale() {
        setTitle(Localizable.get("settings.jre.window.label"));
    }
}
