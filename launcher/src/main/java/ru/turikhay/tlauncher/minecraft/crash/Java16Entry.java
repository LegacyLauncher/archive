package ru.turikhay.tlauncher.minecraft.crash;

import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.JavaManager;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.ui.loc.Localizable;

import java.util.Locale;
import java.util.regex.Pattern;

public class Java16Entry extends PatternEntry {
    private static final Logger LOGGER = LogManager.getLogger(Java16Entry.class);

    public Java16Entry(CrashManager manager) {
        super(manager, "java16",
                Pattern.compile("^Exception in thread \"main\" java.lang.UnsupportedClassVersionError" +
                        ": .+ has been compiled by a more recent version" +
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

        String jreType = TLauncher.getInstance().getSettings()
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
                TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .jre.setValue(JavaManagerConfig.Recommended.TYPE);
                TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .saveValues();
                TLauncher.getInstance().getUIListeners().getMinecraftUIListener()
                        .getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
                TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.startLauncher();
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
            LOGGER.warn("Can't parse class file version: {}", input, e);
            return Localizable.get(getLocPath("version.unknown"));
        }
        /*
            Latest known Java is Java 18. Its classes use version 62.0.
            Every major release that came before Java 18 incremented its class version compared to previous release.
            I mean 1.8 uses 52.0, 9 uses 53.0, 10 uses 54.0 and so on.
            I believe this trend will continue with every major release that will come after Java 18:
            Java 19 will use 63.0, Java 20 - 64.0 and so on.
            Difference between major Java release and its class version is 44.
            With this in mind we can guess what Java version the class requires.
            Reference: https://javaalmanac.io/bytecode/versions/
         */
        return String.format(
                Locale.ROOT,
                "%.0f%s", // -> "8", "18", "19 (probably)", "20 (probably)", ...
                classFileVersion - 44.,
                classFileVersion > 62 ? " " + Localizable.get(getLocPath("version.guessed")) : ""
        );
    }
}
