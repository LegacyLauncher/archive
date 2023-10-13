package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.json.Json;
import net.legacylauncher.bootstrap.meta.LauncherMeta;
import net.legacylauncher.bootstrap.meta.LocalLauncherMeta;
import net.legacylauncher.bootstrap.meta.OldLauncherMeta;
import net.legacylauncher.bootstrap.util.OS;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalLauncher extends Launcher {

    public static final String ENTRY_NAME = "META-INF/launcher-meta.json";
    public static final String OLD_ENTRY_NAME = "ru/turikhay/tlauncher/meta.json";
    private final Path file, libFolder;
    private LocalLauncherMeta meta;

    public LocalLauncher(Path file, Path libFolder) throws LauncherNotFoundException {
        this.file = Objects.requireNonNull(file, "file");
        if (!Files.exists(file)) {
            throw new LauncherNotFoundException("local");
        }
        this.libFolder = Objects.requireNonNull(libFolder, "libFolder");
    }

    public final Path getFile() {
        return file;
    }

    public final Path getLibFolder() {
        return libFolder;
    }

    @Override
    public LocalLauncherMeta getMeta() throws IOException {
        LocalLauncherMeta cachedMeta = this.meta;
        if (cachedMeta != null) return cachedMeta;
        try {
            cachedMeta = findMetaEntry(file, ENTRY_NAME, LocalLauncherMeta.class);
        } catch (IOException e) {
            try {
                cachedMeta = findMetaEntry(file, OLD_ENTRY_NAME, OldLauncherMeta.class).toModernMeta();
            } catch (IOException e1) {
                e1.addSuppressed(e);
                throw e1;
            }
        }
        if (cachedMeta != null) {
            this.meta = cachedMeta;
        } else {
            throw new IOException("Unable to find launcher meta");
        }
        return meta;
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("meta", meta)
                .append("file", file == null ? null : file.toAbsolutePath())
                .append("libFolder", libFolder == null ? null : libFolder.toAbsolutePath());
    }

    public static Path getDefaultFileLocation(String shortBrand) {
        return OS.getDefaultFolder().resolve("bin").resolve(shortBrand.toLowerCase(java.util.Locale.ROOT) + ".jar");
    }

    public static Path getDefaultLibLocation() {
        return OS.getDefaultFolder().resolve("lib");
    }

    static <T extends LauncherMeta> T findMetaEntry(Path file, String entryName, Class<T> clazz) throws IOException {
        if (!Files.exists(file)) {
            throw new FileNotFoundException();
        }
        InputStream input;
        if (Files.isRegularFile(file)) {
            input = getZipEntry(file, entryName);
        } else {
            file = file.resolve(entryName);
            if (!Files.isRegularFile(file)) {
                throw new FileNotFoundException("target entry is not found: " + file.toAbsolutePath());
            }
            input = Files.newInputStream(file);
        }
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return Json.get().fromJson(reader, clazz);
        }
    }

    private static InputStream getZipEntry(Path file, String entryName) throws IOException {
        final ZipFile zip = new ZipFile(file.toFile());
        ZipEntry metaEntry = zip.getEntry(entryName);

        if (metaEntry == null) {
            IOException ioE = new IOException("could not find entry: " + entryName);
            try {
                zip.close();
            } catch (IOException suppressed) {
                ioE.addSuppressed(suppressed);
            }
            throw ioE;
        }

        return new FilterInputStream(zip.getInputStream(metaEntry)) {
            @Override
            public void close() throws IOException {
                super.close();
                zip.close();
            }
        };
    }
}
