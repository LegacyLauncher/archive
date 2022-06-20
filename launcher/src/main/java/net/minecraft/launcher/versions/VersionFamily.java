package net.minecraft.launcher.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.launcher.versions.CompleteVersion.FABRIC_PREFIX;
import static net.minecraft.launcher.versions.CompleteVersion.FORGE_PREFIX;

public class VersionFamily {
    private static final Map<String, Guess> ID_CACHE = new ConcurrentHashMap<>();

    public static Guess guessFamilyOf(VersionSyncInfo versionSyncInfo) {
        if (versionSyncInfo.getLocalCompleteVersion() != null) {
            return new Guess(versionSyncInfo.getLocalCompleteVersion().getFamily(), true);
        }
        String id = versionSyncInfo.getAvailableVersion().getID();
        Guess maybeCached = ID_CACHE.get(id);
        if (maybeCached != null) {
            return maybeCached;
        }
        Guess guess = doGuess(versionSyncInfo, id);
        if (guess != null) {
            ID_CACHE.put(id, guess);
        }
        return guess;
    }

    private static Guess doGuess(VersionSyncInfo versionSyncInfo, String id) {
        switch (versionSyncInfo.getAvailableVersion().getReleaseType()) {
            case UNKNOWN:
            case OLD_ALPHA:
            case SNAPSHOT:
                return new Guess(versionSyncInfo.getAvailableVersion().getReleaseType().toString(), true);
        }
        if (id.toLowerCase(java.util.Locale.ROOT).contains("forge")) {
            return new Guess(FORGE_PREFIX + "???", false);
        }
        if (id.toLowerCase(java.util.Locale.ROOT).contains("fabric")) {
            return new Guess(FABRIC_PREFIX + "???", false);
        }
        String family = CompleteVersion.getFamilyOf(id);
        return family == null ? null : new Guess(family, true);
    }

    public static class Guess {
        private final String family;
        private final boolean confident;

        public Guess(String family, boolean confident) {
            this.family = family;
            this.confident = confident;
        }

        public String getFamily() {
            return family;
        }

        public boolean isConfident() {
            return confident;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Guess guess = (Guess) o;
            return confident == guess.confident && family.equals(guess.family);
        }

        @Override
        public int hashCode() {
            return Objects.hash(family, confident);
        }

        @Override
        public String toString() {
            return "Guess{" +
                    "family='" + family + '\'' +
                    ", confident=" + confident +
                    '}';
        }
    }
}
