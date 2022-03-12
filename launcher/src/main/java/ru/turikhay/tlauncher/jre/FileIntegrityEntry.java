package ru.turikhay.tlauncher.jre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.FileUtil;

import java.io.File;
import java.util.Objects;

public class FileIntegrityEntry {
    private static final Logger LOGGER = LogManager.getLogger(FileIntegrityEntry.class);

    private final String path;
    private final String sha1;
    private final long lastModifiedNanos;

    public FileIntegrityEntry(String path, String sha1, long lastModifiedNanos) {
        this.path = Objects.requireNonNull(path);
        this.sha1 = Objects.requireNonNull(sha1);
        this.lastModifiedNanos = lastModifiedNanos;
    }

    public File resolve(File directory) {
        return new File(Objects.requireNonNull(directory), path);
    }

    public boolean isTamperedWithAt(File directory) {
        File file = resolve(directory);
        if (!file.isFile()) {
            LOGGER.debug("File doesn't exist: {}", file.getAbsolutePath());
            return true;
        }
        String sha1 = FileUtil.getSHA(file);
        if (this.sha1.equals(sha1)) {
            return false;
        }
        LOGGER.debug("File was tampered with: {} (got {}, expected {})", file.getAbsolutePath(), sha1, this.sha1);
        return true;
        /* TODO lastModified может быть записан с разной точностью в зависимости от платформы.
                пока не проверяем.
         */
        /*long lastModified;
        try {
            lastModified = Files.getLastModifiedTime(file.toPath()).to(TimeUnit.NANOSECONDS);
        } catch(IOException ioE) {
            LOGGER.warn("Couldn't get last modified time for {}: {}", file.getAbsolutePath(), ioE.toString());
            lastModified = 0L;
        }
        if(this.lastModifiedNanos == lastModified) {
            return true;
        }*/
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileIntegrityEntry that = (FileIntegrityEntry) o;

        if (lastModifiedNanos != that.lastModifiedNanos) return false;
        if (!path.equals(that.path)) return false;
        return sha1.equals(that.sha1);
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + sha1.hashCode();
        result = 31 * result + (int) (lastModifiedNanos ^ (lastModifiedNanos >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return path + " /#// " + sha1 + " " + lastModifiedNanos;
    }
}
