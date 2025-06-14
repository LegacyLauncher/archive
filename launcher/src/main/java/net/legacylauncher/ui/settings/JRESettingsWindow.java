package net.legacylauncher.ui.settings;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.jre.JavaRuntimeLocal;
import net.legacylauncher.managers.JavaManagerConfig;
import net.legacylauncher.ui.editor.EditorComboBox;
import net.legacylauncher.ui.editor.EditorFileField;
import net.legacylauncher.ui.explorer.FileExplorer;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.*;
import net.legacylauncher.ui.swing.DocumentChangeListener;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedFrame;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.util.shared.JavaVersion;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
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
    private final EditorComboBox<JavaManagerConfig.OptimizedArgsType> optimizedArgsComboBox;
    private final LocalizableLabel settingsSavedLabel;
    private final LocalizableTextField wrapperCommandField;
    private final LocalizableCheckbox useCurrentTrustStoreCheckbox;

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
        optimizedArgsComboBox = createOptimizedArgsCombobox();
        useCurrentTrustStoreCheckbox = createUseCurrentTrustStoreCheckBox();
        initJvmArgs(cfgs, c);
        mcArgsField = addConfig(cfgs, c, "mc-args");
        wrapperCommandField = addConfig(cfgs, c, "wrapper-command");

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
        optimizedArgsComboBox.setSelectedValue(javaManagerConfig.getOptimizedArgumentsType());
        jvmArgsField.setValue(javaManagerConfig.getArgs().orElse(null));
        mcArgsField.setValue(javaManagerConfig.getMinecraftArgs().orElse(null));
        wrapperCommandField.setValue(javaManagerConfig.getWrapperCommand().orElse(null));
        useCurrentTrustStoreCheckbox.setState(javaManagerConfig.getUseCurrentTrustStore());

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
        javaManagerConfig.setWrapperCommand(wrapperCommandField.getValue());
        javaManagerConfig.setOptimizedArgumentsType(optimizedArgsComboBox.getSelectedValue());
        javaManagerConfig.setUseCurrentTrustStore(useCurrentTrustStoreCheckbox.getState());
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
            javaVersion = LegacyLauncher.getInstance().getJavaManager()
                    .getFallbackRecommendedVersion(localCompleteVersion, false);

            if (javaVersion == null) {
                pathMessage.setText("settings.jre.window.configure.path.recommended.current",
                        localCompleteVersion.getID());
                showPath("pathMessage");
                return;
            }
        }

        Optional<JavaRuntimeLocal> localRuntimeOpt = LegacyLauncher.getInstance().getJavaManager().getDiscoverer()
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
                OS.openLink("https://docs." + (comboBox.sp.global.isLikelyRussianSpeakingLocale() ? "legacylauncher.ru" : "llaun.ch/en") + "/faq/custom-java");
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

    private LocalizableCheckbox createUseCurrentTrustStoreCheckBox() {
        LocalizableCheckbox c = new LocalizableCheckbox(
                "settings.jre.window.configure.jvm-args.use-current-trust-store");
        c.addActionListener(e -> saveSelfValues());
        return c;
    }


    private EditorComboBox<JavaManagerConfig.OptimizedArgsType> createOptimizedArgsCombobox() {
        EditorComboBox<JavaManagerConfig.OptimizedArgsType> c = new EditorComboBox<>(
                new OptimizedArgsConverter(),
                JavaManagerConfig.OptimizedArgsType.values()
        );
        c.addActionListener(e -> saveSelfValues());
        return c;
    }

    private void initJvmArgs(ExtendedPanel cfgs, GridBagConstraints c) {
        addConfig(cfgs, c, "jvm-args", jvmArgsField, GridBagConstraints.LINE_START);
        addConfig(cfgs, c, null, useCurrentTrustStoreCheckbox, GridBagConstraints.LINE_START);
        addConfig(cfgs, c, "jvm-args.improved", optimizedArgsComboBox, GridBagConstraints.LINE_START);
    }

    private void addConfig(ExtendedPanel p, GridBagConstraints c, String path, JComponent component, int anchor) {
        c.gridy++;

        if (path != null) {
            LocalizableLabel label = new LocalizableLabel("settings.jre.window.configure." + path);

            c.gridx = 0;
            c.anchor = anchor;
            c.insets = SwingUtil.magnify(new Insets(10, 0, 0, 10));
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0.0;
            p.add(label, c);
        }

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

    private static class OptimizedArgsConverter extends LocalizableStringConverter<JavaManagerConfig.OptimizedArgsType> {
        public OptimizedArgsConverter() {
            super("settings.jre.window.configure.jvm-args.type");
        }

        @Override
        protected String toPath(JavaManagerConfig.OptimizedArgsType var1) {
            return var1.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public JavaManagerConfig.OptimizedArgsType fromString(String var1) {
            return JavaManagerConfig.OptimizedArgsType.parse(var1.toUpperCase(Locale.ROOT));
        }

        @Override
        public String toValue(JavaManagerConfig.OptimizedArgsType var1) {
            return var1.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public Class<JavaManagerConfig.OptimizedArgsType> getObjectClass() {
            return JavaManagerConfig.OptimizedArgsType.class;
        }
    }
}
