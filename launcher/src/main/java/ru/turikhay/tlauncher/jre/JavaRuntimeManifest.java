package ru.turikhay.tlauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.updater.DownloadInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.downloader.Downloadable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaRuntimeManifest {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeManifest.class);

    private final List<RuntimeFile> files;

    JavaRuntimeManifest(List<RuntimeFile> files) {
        this.files = files;
    }

    public List<RuntimeFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public long countBytes() {
        return files.stream().filter(RuntimeFile::isFile).mapToLong(file -> file.getDownload().getSize()).count();
    }

    public List<Downloadable> toDownloadableList(File workingDir, boolean forceDownload) {
        return files.stream()
                .filter(RuntimeFile::isFile)
                .map(file -> file.createDownloadable(workingDir, forceDownload))
                .collect(Collectors.toList());
    }

    public List<FileIntegrityEntry> toIntegrityEntries() {
        return files.stream()
                .filter(RuntimeFile::isFile)
                .map(RuntimeFile::toIntegrityEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static class RuntimeFile {
        private String path;
        private String type;
        private Map<String, DownloadInfo> downloads; // known keys: lzma, raw
        private boolean executable;

        public String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        public boolean isExecutable() {
            return executable;
        }

        public String getType() {
            return type;
        }

        public boolean isFile() {
            return "file".equals(type);
        }

        public boolean isDirectory() {
            return "directory".equals(type);
        }

        public boolean hasLzmaDownload() {
            return downloads.containsKey("lzma");
        }

        public DownloadInfo getLzmaDownload() {
            return downloads.get("lzma");
        }

        public DownloadInfo getDownload() {
            return downloads.get("raw");
        }

        public Downloadable createDownloadable(File workingDir, boolean forceDownload) {
            File destination = new File(workingDir, path);
            boolean isLzma = hasLzmaDownload();
            return new JavaRuntimeFileDownloadable(
                    isLzma ? getLzmaDownload() : getDownload(),
                    isLzma,
                    destination,
                    executable,
                    forceDownload
            );
        }

        FileIntegrityEntry toIntegrityEntry() {
            DownloadInfo download = getDownload();
            if (download == null) {
                LOGGER.warn("file doesn't contain raw download: {}", this);
                return null;
            }
            return new FileIntegrityEntry(
                    path,
                    download.getSha1(),
                    0L
            );
        }

        @Override
        public String toString() {
            return "RuntimeFile{" +
                    "path='" + path + '\'' +
                    ", type='" + type + '\'' +
                    ", downloads=" + downloads +
                    ", executable=" + executable +
                    '}';
        }
    }

    private static Gson GSON;

    static Gson getGson() {
        if (GSON == null) {
            GSON = new GsonBuilder()
                    .registerTypeAdapter(JavaRuntimeManifest.class, new JavaRuntimeManifestDeserializer())
                    .create();
        }
        return GSON;
    }
}
