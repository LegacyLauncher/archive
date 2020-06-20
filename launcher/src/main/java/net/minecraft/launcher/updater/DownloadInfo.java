package net.minecraft.launcher.updater;

import java.net.URL;

public class DownloadInfo {
    protected String url;
    protected String sha1;
    protected int size;

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public int getSize() {
        return size;
    }
}
