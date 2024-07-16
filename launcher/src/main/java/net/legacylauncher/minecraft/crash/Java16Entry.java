package net.legacylauncher.minecraft.crash;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.managers.JavaManager;
import net.legacylauncher.managers.JavaManagerConfig;
import net.legacylauncher.ui.loc.Localizable;
import net.minecraft.launcher.versions.CompleteVersion;

import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
public class Java16Entry extends PatternEntry {
    public Java16Entry(CrashManager manager) {
        super(manager, "java16",
                Pattern.compile(".*java.lang.UnsupportedClassVersionError: .+ has been compiled by a more recent version" +
                        " of the Java Runtime \\(class file version (?<classFileVersion>.+)\\), this " +
                        "version of the Java Runtime only recognizes class file versions up to .+$")
        );
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if (!super.checkCapability()) {
            return false;
        }

        CompleteVersion version = getManager().getLauncher().getCompleteVersion();

        final String requiredJavaVersion;
        if (getMatch() == null) {
            requiredJavaVersion = "???";
        } else {
            requiredJavaVersion = guessJavaVersionFromClassFileVersion(getMatch().group("classFileVersion"));
        }

        String jreType = LegacyLauncher.getInstance().getSettings()
                .get(JavaManagerConfig.class).getJreTypeOrDefault().getType();

        if (jreType.equals(JavaManagerConfig.Recommended.TYPE)) {
            // we use recommended jre
            if (version.getReleaseTime() != null
                    && version.getReleaseTime().toInstant().compareTo(JavaManager.JAVA16_UPGRADE_POINT) >= 0) {
                // this version released after the Java 16 upgrade point
                if (getManager().getLauncher().getJreType().getType()
                        .equals(JavaManagerConfig.Current.TYPE)) {
                    // but the launcher used current JRE
                    if (version.getJavaVersion() == null) {
                        // version has no java version requirement
                        // => an old launcher version corrupted json file
                        setPath("force-update-version");
                        addButton(getManager().getButton("force-update"));
                        return true;
                    }
                }
            } else {
                // but the version released before the upgrade point
                // => ???
                setPath("old-java", requiredJavaVersion);
            }
        } else {
            // => user probably has no idea what they're doing
            setPath("change-to-recommended", requiredJavaVersion);
            newButton("retry-with-recommended", () -> {
                LegacyLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .jre.setValue(JavaManagerConfig.Recommended.TYPE);
                LegacyLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .saveValues();
                LegacyLauncher.getInstance().getUiListeners().getMinecraftUIListener()
                        .getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
                LegacyLauncher.getInstance().getFrame().mp.defaultScene.loginForm.startLauncher();
            });
            setPermitHelp(false);
        }
        return true;
    }

    private String guessJavaVersionFromClassFileVersion(String input) {
        double classFileVersion;
        try {
            classFileVersion = Double.parseDouble(input);
            if (classFileVersion < 52) {
                throw new RuntimeException("class file version is too old: " + classFileVersion);
            }
        } catch (Exception e) {
            log.warn("Can't parse class file version: {}", input, e);
            return Localizable.get("crash.java16.old-java.version.unknown");
        }
        int guessedJavaVersion = (int) (classFileVersion - 44);
        if (guessedJavaVersion > 21) {
            // Java 42 (probably)
            return String.format(Locale.ROOT, "%d %s", guessedJavaVersion, Localizable.get("crash.java16.old-java.version.guessed"));
        } else {
            // Java 21
            return String.valueOf(guessedJavaVersion);
        }
    }
}
