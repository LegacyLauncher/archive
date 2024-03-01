package net.minecraft.launcher.updater;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class StreamVersionList extends VersionList {
    protected InputStreamReader getUrl(String uri) throws IOException {
        return new InputStreamReader(getInputStream(uri), StandardCharsets.UTF_8);
    }

    protected abstract InputStream getInputStream(String var1) throws IOException;
}
