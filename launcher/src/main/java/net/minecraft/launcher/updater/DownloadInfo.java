package net.minecraft.launcher.updater;

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


    @Override
    public String toString() {
        return "DownloadInfo{" +
                "url='" + url + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                '}';
    }
}
