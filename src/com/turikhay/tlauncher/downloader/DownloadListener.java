package com.turikhay.tlauncher.downloader;

public interface DownloadListener {
   void onDownloaderStart(Downloader var1, int var2);

   void onDownloaderError(Downloader var1, Downloadable var2, Throwable var3);

   void onDownloaderProgress(Downloader var1, int var2);

   void onDownloaderFileComplete(Downloader var1, Downloadable var2);

   void onDownloaderComplete(Downloader var1);
}
