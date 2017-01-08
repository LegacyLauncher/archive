package net.minecraft.launcher.updater;

import java.net.URL;

public class DownloadInfo {
    protected URL url;
    protected String sha1;
    protected int size;

    public URL getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public int getSize() {
        return size;
    }
}
