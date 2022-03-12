package pw.modder.tl.curse;

import pw.modder.hashing.MurmurHash2;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;

public class AddonFile {
    private int id;
    private String displayName;
    private String fileName;
    private Instant fileDate;
    private long fileLength;
    private AddonFileReleaseType releaseType; // ordinal
    private Status fileStatus; // ordinal
    private String downloadUrl;
    private boolean isAlternate;
    private short alternateFileId;
    private Set<Dependency> dependencies;
    private boolean isAvailable;
    private long packageFingerprint;
    private Set<String> gameVersion;

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public Instant getFileDate() {
        return fileDate;
    }

    public long getFileLength() {
        return fileLength;
    }

    public Status getFileStatus() {
        return fileStatus;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean isAlternate() {
        return isAlternate;
    }

    public short getAlternateFileId() {
        return alternateFileId;
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public long getPackageFingerprint() {
        return packageFingerprint;
    }

    public Set<String> getGameVersion() {
        return gameVersion;
    }

    public boolean hashMatches(long hash) {
        return packageFingerprint == hash;
    }

    public boolean hashMatches(File file) throws IOException {
        if (file == null) return false;
        if (!file.isFile()) return false;

        return MurmurHash2.hash32normalized(file) == packageFingerprint;
    }

    public boolean gameVersionMatches(String version) {
        if (gameVersion.isEmpty()) return false;
        return gameVersion.stream().anyMatch(it -> it.equals(version));
    }

    public static class Dependency {
        private long id;
        private int addonId;
        private DependencyType type; // ordinal
        private int fileId;

        public long getId() {
            return id;
        }

        public int getAddonId() {
            return addonId;
        }

        public DependencyType getType() {
            return type;
        }

        public int getFileId() {
            return fileId;
        }

        public AddonFile getFile() throws IOException {
            return CurseClient.getAddonFile(addonId, fileId);
        }

        public String getDownloadLink() throws IOException {
            return CurseClient.getAddonFileDownloadURL(addonId, fileId);
        }
    }

    public enum Status {
        PROCESSING,
        CHANGES_REQUIRED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        MALWARE_DETECTED,
        DELETED,
        ARCHIVED,
        TESTING,
        RELEASED,
        READY_FOR_REVIEW,
        DEPRECATED,
        BAKING,
        AWAITING_FOR_PUBLISHING,
        FAILED_PUBLISHING
    }

    public enum DependencyType {
        EMBEDDED_LIBRARY,
        OPTIONAL_DEPENDENCY,
        REQUIRED_DEPENDENCY,
        TOOL,
        INCOMPATIBLE,
        INCLUDE
    }
}