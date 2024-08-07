package net.legacylauncher.bootstrap.launcher;

import com.google.gson.JsonSyntaxException;
import net.legacylauncher.bootstrap.meta.LocalLauncherMeta;
import net.legacylauncher.bootstrap.meta.OldLauncherMeta;
import net.legacylauncher.bootstrap.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class InternalLauncher extends LocalCastingLauncher {

    private final URL url;
    private Path tempFile;

    public InternalLauncher() throws LauncherNotFoundException {
        this.url = getClass().getResource("/launcher.jar");
        if (url == null) {
            throw new LauncherNotFoundException("internal");
        }
    }

    private InputStream getInputStream() throws IOException {
        if (tempFile != null) {
            return Files.newInputStream(tempFile);
        }
        return url.openStream();
    }

    private Path getTempFile() throws IOException {
        if (tempFile == null) {
            Path temp = Files.createTempFile("tlauncher", null);
            unpack(Files.newOutputStream(temp));
            this.tempFile = temp;
        }
        return tempFile;
    }

    private void unpack(OutputStream out) throws IOException {
        IOUtils.copy(getInputStream(), out);
    }

    @Override
    public LocalLauncherMeta getMeta() throws IOException, JsonSyntaxException {
        try {
            return LocalLauncher.findMetaEntry(getTempFile(), LocalLauncher.ENTRY_NAME, LocalLauncherMeta.class);
        } catch (IOException e) {
            try {
                return LocalLauncher.findMetaEntry(getTempFile(), LocalLauncher.OLD_ENTRY_NAME, OldLauncherMeta.class).toModernMeta();
            } catch (IOException e1) {
                e1.addSuppressed(e);
                throw e1;
            }
        }
    }

    public Task<LocalLauncher> toLocalLauncher(final Path file, final Path libFolder) {
        return new Task<LocalLauncher>("internalToLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                Files.createDirectories(file.getParent());
                unpack(Files.newOutputStream(file));

                try {
                    return new LocalLauncher(file, libFolder);
                } catch (LauncherNotFoundException lnfE) {
                    throw new IOException(lnfE);
                }
            }
        };
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("url", url)
                .append("tempFile", tempFile);
    }
}
