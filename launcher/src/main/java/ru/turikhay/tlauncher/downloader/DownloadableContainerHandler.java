package ru.turikhay.tlauncher.downloader;

public interface DownloadableContainerHandler {
    default void onStart(DownloadableContainer container) {
    }

    default void onAbort(DownloadableContainer container) {
    }

    default void onError(DownloadableContainer container, Downloadable downloadable, Throwable e) {
    }

    default void onComplete(DownloadableContainer container, Downloadable downloadable) {
    }

    default void onFullComplete(DownloadableContainer container) {
    }
}
