package net.legacylauncher.ui.settings;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.managers.JavaManagerConfig;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.converter.StringConverter;
import net.legacylauncher.ui.editor.EditorComboBox;
import net.legacylauncher.ui.editor.EditorField;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.shared.JavaVersion;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class JREComboBox extends BorderPanel implements EditorField, LocalizableComponent {
    final SettingsPanel sp;
    final JavaVersionDetectorCache javaVersionCache;

    private final EditorComboBox<String> comboBox;
    private final LocalizableButton customizeButton;

    private JRESettingsWindow settingsWindow;

    public JREComboBox(SettingsPanel sp) {
        this.sp = sp;

        this.comboBox = new EditorComboBox<>(
                new JREConverter(),
                JavaManagerConfig.keys().toArray(new String[0])
        );
        this.javaVersionCache = new JavaVersionDetectorCache(this::javaVersionCallback);
        this.customizeButton = new LocalizableButton("settings.jre.button.customize");
        customizeButton.addActionListener(e -> openSettingsWindow());

        setCenter(comboBox);
        setEast(customizeButton);
    }

    public void selectedVersionChanged(VersionSyncInfo versionSyncInfo) {
        comboBox.repaint();
        if (settingsWindow != null) {
            settingsWindow.selectedVersionChanged(versionSyncInfo);
        }
    }

    private void javaVersionCallback() {
        comboBox.repaint();
        if (settingsWindow != null) {
            settingsWindow.javaVersionCallback();
        }
    }

    private void openSettingsWindow() {
        if (settingsWindow == null) {
            settingsWindow = new JRESettingsWindow(this);
            settingsWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            settingsWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    Blocker.unblock("settingsWindow", JREComboBox.this);
                    settingsWindow = null;
                    sp.updateValues();
                }
            });
        }
        settingsWindow.showSettingsWindow();
        Blocker.block("settingsWindow", JREComboBox.this);
    }

    @Override
    public void block(Object var1) {
        comboBox.block(var1);
        customizeButton.setEnabled(false);
    }

    @Override
    public void unblock(Object var1) {
        comboBox.unblock(var1);
        customizeButton.setEnabled(true);
    }

    @Override
    public String getSettingsValue() {
        return comboBox.getSettingsValue();
    }

    @Override
    public void setSettingsValue(String var1) {
        comboBox.setSettingsValue(var1);
    }

    @Override
    public boolean isValueValid() {
        if (!comboBox.isValueValid()) {
            return false;
        }
        JavaManagerConfig.JreType jreType = JavaManagerConfig.createByType(comboBox.getSettingsValue());
        if (jreType instanceof JavaManagerConfig.Custom) {
            JavaManagerConfig.Custom custom = (JavaManagerConfig.Custom) jreType;
            custom.load(sp.global);
            if (!custom.getPath().isPresent()) {
                log.warn("Custom JRE is not configured");
                if (LegacyLauncher.getInstance() != null) {
                    Alert.showError(
                            "",
                            Localizable.get("settings.jre.type.custom.not-configured")
                    );
                }
                return false;
            }
        }
        return true;
    }

    private String getJavaVersion(String path) {
        Future<JavaVersion> javaVersionFuture = javaVersionCache.get(path);
        if (!javaVersionFuture.isDone()) {
            return "...";
        } else {
            try {
                return javaVersionFuture.get().getVersion();
            } catch (InterruptedException | ExecutionException e) {
                return Localizable.get("settings.jre.type.custom.unknown-version");
            }
        }
    }

    @Override
    public void updateLocale() {
        if (settingsWindow != null) {
            Localizable.updateContainer(settingsWindow);
        }
    }

    private class JREConverter implements StringConverter<String> {
        @Override
        public String fromString(String var1) {
            return var1;
        }

        @Override
        public String toString(String var1) {
            JavaManagerConfig.JreType jreType;
            try {
                jreType = JavaManagerConfig.createByType(var1);
            } catch (RuntimeException e) {
                return var1;
            }
            if (jreType instanceof JavaManagerConfig.Current) {
                return Localizable.get("settings.jre.type." + var1,
                        "Java " + OS.JAVA_VERSION.getMajor());
            }
            if (jreType instanceof JavaManagerConfig.Recommended) {
                String value = Localizable.get("settings.jre.type." + var1);
                VersionSyncInfo version = sp.scene.loginForm.versions.getVersion();
                if (version == null) {
                    return value;
                }
                CompleteVersion localCompleteVersion = version.getLocalCompleteVersion();
                if (localCompleteVersion == null) {
                    return value;
                }
                CompleteVersion.JavaVersion javaVersion = localCompleteVersion.getJavaVersion();
                if (javaVersion == null) {
                    javaVersion = LegacyLauncher.getInstance().getJavaManager()
                            .getFallbackRecommendedVersion(localCompleteVersion, false);
                }
                return value + " (" + Localizable.get("settings.jre.type.recommended.specific",
                        localCompleteVersion.getID(),
                        javaVersion == null ?
                                Localizable.get("settings.jre.type.current.lowercase")
                                : javaVersion.getComponent()
                ) + ")";
            }
            if (jreType instanceof JavaManagerConfig.Custom) {
                JavaManagerConfig.Custom custom = (JavaManagerConfig.Custom) jreType;
                custom.load(sp.global);
                Optional<String> path = ((JavaManagerConfig.Custom) jreType).getPath();
                return Localizable.get("settings.jre.type." + var1) +
                        path.map(s -> " (" + getJavaVersion(s) + ")").orElse("");
            }
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
    }
}
