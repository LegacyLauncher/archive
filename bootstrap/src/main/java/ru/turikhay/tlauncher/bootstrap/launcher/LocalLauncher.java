package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.meta.LauncherMeta;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.LocalLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalLauncher extends Launcher {

    private final File file, libFolder;
    private LocalLauncherMeta meta;

    public LocalLauncher(File file, File libFolder) throws LauncherNotFoundException {
        this.file = U.requireNotNull(file, "file");
        if(!file.exists()) {
            throw new LauncherNotFoundException("local");
        }
        this.libFolder = U.requireNotNull(libFolder, "libFolder");
    }

    public final File getFile() {
        return file;
    }

    public final File getLibFolder() {
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
                .append("file", file == null ? null : file.getAbsolutePath())
                .append("libFolder", libFolder == null ? null : libFolder.getAbsolutePath());
    }

    private static File getDefaultLocation() {
        return OS.getSystemRelatedDirectory("tlauncher", true);
    }

    public static File getDefaultFileLocation(String shortBrand) {
        return new File(getDefaultLocation(), "bin/"+ shortBrand.toLowerCase() +".jar");
    }

    public static File getDefaultLibLocation() {
        return new File(getDefaultLocation(), "lib");
    }

    static <T extends LauncherMeta> T findMetaEntry(File file, Class<T> clazz) throws IOException {
        final String entryName = "ru/turikhay/tlauncher/meta.json";

        if(!file.exists()) {
            throw new FileNotFoundException();
        }

        InputStream input;
        if(file.isFile()) {
            input = getZipEntry(file, entryName);
        } else {
            file = new File(file, entryName);
            if(!file.isFile()) {
                throw new FileNotFoundException("target entry is not found: " + file.getAbsolutePath());
            }
            input = new FileInputStream(file);
        }

        return Json.get().fromJson(new InputStreamReader(input, U.UTF8), clazz);
    }

    private static InputStream getZipEntry(File file, String entryName) throws IOException {
        final ZipFile zip = new ZipFile(file);
        ZipEntry metaEntry = zip.getEntry(entryName);

        if (metaEntry == null) {
            throw new IOException("could not find entry: " + entryName);
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
