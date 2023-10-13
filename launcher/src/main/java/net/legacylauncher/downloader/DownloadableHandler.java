package net.legacylauncher.downloader;

public interface DownloadableHandler {
    void onStart(Downloadable var1);

    void onAbort(Downloadable var1);

    void onComplete(Downloadable var1);

    void onError(Downloadable var1, Throwable var2);
}
