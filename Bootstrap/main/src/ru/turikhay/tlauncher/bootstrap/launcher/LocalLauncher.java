package ru.turikhay.tlauncher.bootstrap.launcher;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.LocalLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalLauncher extends Launcher {

    private final File file;

    public LocalLauncher(File file) {
        this.file = file;
    }

    public final File getFile() {
        return file;
    }

    @Override
    public LocalLauncherMeta getMeta() throws IOException {
        final String entryName = "meta.json";

        final ZipFile zip = new ZipFile(file);
        ZipEntry metaEntry = zip.getEntry(entryName);

        if (metaEntry == null) {
            throw new IOException("could not find entry: " + entryName);
        }

        return Json.get().fromJson(new InputStreamReader(zip.getInputStream(metaEntry), U.UTF8) {
            @Override
            public void close() throws IOException {
                super.close();
                zip.close();
            }
        }, LocalLauncherMeta.class);
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("file", file == null ? null : file.getAbsolutePath());
    }

    public static File getDefaultLocation(String shortBrand) {
        return new File(OS.getSystemRelatedDirectory("tlauncher", true), "bin/"+ shortBrand.toLowerCase() +".jar");
    }
}
