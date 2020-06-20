package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.*;
import java.net.URL;

public class InternalLauncher extends LocalCastingLauncher {

    private final URL url;
    private File tempFile;

    public InternalLauncher() throws LauncherNotFoundException {
        this.url = getClass().getResource("/launcher.jar");
        if(url == null) {
            throw new LauncherNotFoundException("internal");
        }
    }

    private InputStream getInputStream() throws IOException {
        if(tempFile != null) {
            return new FileInputStream(tempFile);
        }
        return url.openStream();
    }

    private File getTempFile() throws IOException {
        if(tempFile == null) {
            File temp = File.createTempFile("tlauncher", null);
            unpack(new FileOutputStream(temp));
            this.tempFile = temp;
        }
        return tempFile;
    }

    private void unpack(OutputStream out) throws IOException {
        IOUtils.copy(getInputStream(), out);
    }

    @Override
    public InternalLauncherMeta getMeta() throws IOException, JsonSyntaxException {
        return LocalLauncher.findMetaEntry(getTempFile(), InternalLauncherMeta.class);
    }

    public Task<LocalLauncher> toLocalLauncher(final File file, final File libFolder) {
        return new Task<LocalLauncher>("internalToLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                U.createFile(file);
                unpack(new FileOutputStream(file));

                try {
                    return new LocalLauncher(file, libFolder);
                } catch(LauncherNotFoundException lnfE) {
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
