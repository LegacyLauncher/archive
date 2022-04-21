package ru.turikhay.tlauncher.downloader;

public interface DownloadableContainerHandler {
    default void onStart(DownloadableContainer var1) {
    }

    default void onAbort(DownloadableContainer var1) {
    }

    default void onError(DownloadableContainer var1, Downloadable var2, Throwable var3) {
    }

    default void onComplete(DownloadableContainer var1, Downloadable var2) {
    }

    default void onFullComplete(DownloadableContainer var1) {
    }
}
