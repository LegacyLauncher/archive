package net.legacylauncher.downloader;

public interface DownloaderListener {
    void onDownloaderStart(Downloader downloader, int tasks);

    void onDownloaderAbort(Downloader downloader);

    void onDownloaderProgress(Downloader downloader, double progress, double speed);

    void onDownloaderFileComplete(Downloader downloader, Downloadable downloadable);

    void onDownloaderComplete(Downloader downloader);
}
