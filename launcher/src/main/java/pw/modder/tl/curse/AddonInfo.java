package pw.modder.tl.curse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class AddonInfo {
    private int id;
    private String name;
    private Set<Author> authors;
    private String websiteUrl;
    private int gameId; // always 432
    private String summary;
    private int defaultFileId;
    private long downloadCount;
    private Set<AddonFile> latestFiles;
    private Status status; // by ordinal
    private String slug;
    private Set<LatestFile> gameVersionLatestFiles;
    private Instant dateModified;
    private Instant dateCreated;
    private Instant dateReleased;
    private boolean isAvailable;
    private boolean isExperiemental;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public int getGameId() {
        return gameId;
    }

    public String getSummary() {
        return summary;
    }

    public int getDefaultFileId() {
        return defaultFileId;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public Set<AddonFile> getLatestFiles() {
        return latestFiles;
    }

    public Status getStatus() {
        return status;
    }

    public String getSlug() {
        return slug;
    }

    public Set<LatestFile> getGameVersionLatestFiles() {
        return gameVersionLatestFiles;
    }

    public Instant getDateModified() {
        return dateModified;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public Instant getDateReleased() {
        return dateReleased;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean isExperiemental() {
        return isExperiemental;
    }

    public AddonFile getFile(LatestFile file) throws IOException {
        return CurseClient.getAddonFile(id, file.getProjectFileId());
    }

    public AddonFile getFile(int fileId) throws IOException {
        return CurseClient.getAddonFile(id, fileId);
    }

    public List<AddonFile> getFiles() throws IOException {
        return CurseClient.getAddonFiles(id);
    }

    public String getFileDownloadLink(int fileId) throws IOException {
        return CurseClient.getAddonFileDownloadURL(id, fileId);
    }

    public static class LatestFile {
        private String gameVersion;
        private int projectFileId;
        private String projectFileName;
        private AddonFileReleaseType fileType; // ordinal

        public String getGameVersion() {
            return gameVersion;
        }

        public int getProjectFileId() {
            return projectFileId;
        }

        public String getProjectFileName() {
            return projectFileName;
        }

        public AddonFileReleaseType getFileType() {
            return fileType;
        }
    }

    public static class Author {
        private String name;
        private String url;
        private int projectId;
        private int id;
        private int userId;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public int getProjectId() {
            return projectId;
        }

        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }
    }

    public enum Status {
        NEW,
        CHANGES_REQUIRED,
        UNDER_SOFT_REVIEW,
        APPROVED,
        REJECTED,
        CHANGES_MADE,
        INACTIVE,
        ABANDONED,
        DELETED,
        UNDER_REVIEW
    }
}
