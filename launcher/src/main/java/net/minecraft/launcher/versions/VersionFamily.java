package net.minecraft.launcher.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.launcher.versions.CompleteVersion.FABRIC_PREFIX;
import static net.minecraft.launcher.versions.CompleteVersion.FORGE_PREFIX;

public class VersionFamily {
    private static final Map<String, String> ID_CACHE = new ConcurrentHashMap<>();

    public static String guessFamilyOf(VersionSyncInfo versionSyncInfo) {
        if (versionSyncInfo.getLocalCompleteVersion() != null) {
            return versionSyncInfo.getLocalCompleteVersion().getFamily();
        }
        String id = versionSyncInfo.getAvailableVersion().getID();
        String maybeCached = ID_CACHE.get(id);
        if (maybeCached != null) {
            return maybeCached;
        }
        String family = doGuess(versionSyncInfo, id);
        if (family != null) {
            ID_CACHE.put(id, family);
        }
        return family;
    }

    private static String doGuess(VersionSyncInfo versionSyncInfo, String id) {
        switch (versionSyncInfo.getAvailableVersion().getReleaseType()) {
            case UNKNOWN:
            case OLD_ALPHA:
            case SNAPSHOT:
                return versionSyncInfo.getAvailableVersion().getReleaseType().toString();
        }
        if (id.toLowerCase(java.util.Locale.ROOT).contains("forge")) {
            return FORGE_PREFIX + "???";
        }
        if (id.toLowerCase(java.util.Locale.ROOT).contains("fabric")) {
            return FABRIC_PREFIX + "???";
        }
        return CompleteVersion.getFamilyOf(id);
    }
}
