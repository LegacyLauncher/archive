package pw.modder.tl.curse;

import java.io.IOException;
import java.util.Set;

public class ModpackManifest {
    private MinecraftData minecraft;
    private String manifestType; // should be only minecraftModpack
    private short manifestVersion; // 1
    private String name;
    private String version;
    private String author;
    private Set<ModFile> files;
    private String overrides;

    public MinecraftData getMinecraft() {
        return minecraft;
    }

    public String getManifestType() {
        return manifestType;
    }

    public short getManifestVersion() {
        return manifestVersion;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public Set<ModFile> getFiles() {
        return files;
    }

    public String getOverrides() {
        return overrides;
    }

    public static class MinecraftData {
        private String version;
        private Set<ModloaderData> modloaders; // example: "forge-36.0.21"

        public String getVersion() {
            return version;
        }

        public Set<ModloaderData> getModloaders() {
            return modloaders;
        }
    }

    public static class ModloaderData {
        private String id;
        private boolean primary;

        public String getId() {
            return id;
        }

        public boolean isPrimary() {
            return primary;
        }
    }

    public static class ModFile {
        private int projectID;
        private int fileID;
        private boolean required;

        private Long checksum = null;       // manually receive and save checksum
        private String downloadUrl = null;  // and download url

        public void fetchFile() throws IOException {
            AddonFile file = getAddonFile();
            checksum = file.getPackageFingerprint();
            downloadUrl = file.getDownloadUrl();
        }

        public Long getChecksum() {
            return checksum;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public int getProjectID() {
            return projectID;
        }

        public int getFileID() {
            return fileID;
        }

        public boolean isRequired() {
            return required;
        }

        public AddonFile getAddonFile() throws IOException {
            return CurseClient.getAddonFile(projectID, fileID);
        }

        public String getDownloadLink() throws IOException {
            return CurseClient.getAddonFileDownloadURL(projectID, fileID);
        }
    }
}
