package ru.turikhay.tlauncher.downloader;

public interface DownloaderListener {
    void onDownloaderStart(Downloader var1, int var2);

    void onDownloaderAbort(Downloader var1);

    void onDownloaderProgress(Downloader var1, double var2, double var4);

    void onDownloaderFileComplete(Downloader var1, Downloadable var2);

    void onDownloaderComplete(Downloader var1);
}
