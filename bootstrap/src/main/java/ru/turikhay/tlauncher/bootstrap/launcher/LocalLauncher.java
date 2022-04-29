package ru.turikhay.tlauncher.bootstrap.launcher;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.LauncherMeta;
import ru.turikhay.tlauncher.bootstrap.meta.LocalLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.util.OS;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalLauncher extends Launcher {

    public static final String ENTRY_NAME = "ru/turikhay/tlauncher/meta.json";
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
        return findMetaEntry(file, LocalLauncherMeta.class);
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

    static <T extends LauncherMeta> T findMetaEntry(Path file, Class<T> clazz) throws IOException {
        if (!Files.exists(file)) {
            throw new FileNotFoundException();
        }
        InputStream input;
        if (Files.isRegularFile(file)) {
            input = getZipEntry(file);
        } else {
            file = file.resolve(ENTRY_NAME);
            if (!Files.isRegularFile(file)) {
                throw new FileNotFoundException("target entry is not found: " + file.toAbsolutePath());
            }
            input = Files.newInputStream(file);
        }
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return Json.get().fromJson(reader, clazz);
        }
    }

    private static InputStream getZipEntry(Path file) throws IOException {
        final ZipFile zip = new ZipFile(file.toFile());
        ZipEntry metaEntry = zip.getEntry(ENTRY_NAME);

        if (metaEntry == null) {
            IOException ioE = new IOException("could not find entry: " + ENTRY_NAME);
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
