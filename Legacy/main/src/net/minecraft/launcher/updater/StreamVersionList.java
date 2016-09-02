package net.minecraft.launcher.updater;

import ru.turikhay.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class StreamVersionList extends VersionList {
    protected InputStreamReader getUrl(String uri) throws IOException {
        return new InputStreamReader(getInputStream(uri), FileUtil.DEFAULT_CHARSET);
    }

    protected abstract InputStream getInputStream(String var1) throws IOException;
}
