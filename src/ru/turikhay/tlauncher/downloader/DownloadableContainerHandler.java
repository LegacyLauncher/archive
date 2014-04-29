package ru.turikhay.tlauncher.downloader;

public interface DownloadableContainerHandler {
   void onStart(DownloadableContainer var1);

   void onAbort(DownloadableContainer var1);

   void onError(DownloadableContainer var1, Downloadable var2, Throwable var3);

   void onComplete(DownloadableContainer var1, Downloadable var2) throws RetryDownloadException;

   void onFullComplete(DownloadableContainer var1);
}
