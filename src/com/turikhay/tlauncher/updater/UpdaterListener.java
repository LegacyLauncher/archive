package com.turikhay.tlauncher.updater;

public interface UpdaterListener {
   void onUpdaterRequesting(Updater var1);

   void onUpdaterRequestError(Updater var1, Throwable var2);

   void onUpdaterFoundUpdate(Updater var1, boolean var2);

   void onUpdaterNotFoundUpdate(Updater var1);

   void onUpdaterDownloading(Updater var1);

   void onUpdaterDownloadSuccess(Updater var1);

   void onUpdaterDownloadError(Updater var1, Throwable var2);

   void onUpdaterProcessError(Updater var1, Throwable var2);
}
