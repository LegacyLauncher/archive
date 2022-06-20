package ru.turikhay.tlauncher.ui.converter;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.VersionFamily;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

import java.io.File;
import java.util.Locale;

import static net.minecraft.launcher.versions.CompleteVersion.FABRIC_PREFIX;
import static net.minecraft.launcher.versions.CompleteVersion.FORGE_PREFIX;

public class SeparateDirsConverter extends LocalizableStringConverter<Configuration.SeparateDirs> {
    private final boolean useCurrentVersionHint;

    public SeparateDirsConverter(boolean useCurrentVersionHint) {
        super("settings.client.gamedir.separatedirs");
        this.useCurrentVersionHint = useCurrentVersionHint;
    }

    public Configuration.SeparateDirs fromString(String from) {
        return Configuration.SeparateDirs.get(from);
    }

    public String toValue(Configuration.SeparateDirs from) {
        return from == null ? null : from.toString().toLowerCase(Locale.ROOT);
    }

    public String toPath(Configuration.SeparateDirs from) {
        return from == null ? null : from.toString().toLowerCase(Locale.ROOT);
    }

    public String toString(Configuration.SeparateDirs from) {
        return super.toString(from) + evaluateHintIfAvailable(from);
    }

    public Class<Configuration.SeparateDirs> getObjectClass() {
        return Configuration.SeparateDirs.class;
    }

    private String evaluateHintIfAvailable(Configuration.SeparateDirs from) {
        if (!useCurrentVersionHint || from == null) {
            return "";
        }
        String hint = evaluateBestEffortHint(from);
        return hint == null ? "" : " (" + hint + ")";
    }

    private static final String homeDirPrefix = "home" + File.separatorChar;

    private static String evaluateBestEffortHint(Configuration.SeparateDirs mode) {
        if (mode == null || mode == Configuration.SeparateDirs.NONE || !TLauncher.getInstance().isReady()) {
            return null;
        }
        VersionSyncInfo versionSyncInfo =
                TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.versions.getVersion();
        if (versionSyncInfo == null) {
            return null;
        }
        switch (mode) {
            case FAMILY:
                VersionFamily.Guess guess = VersionFamily.guessFamilyOf(versionSyncInfo);
                return guess == null ? null : homeDirPrefix + guess.getFamily();
            case VERSION:
                return homeDirPrefix + versionSyncInfo.getAvailableVersion().getID();
        }
        return null;
    }
}
