package com.turikhay.tlauncher.updater;

public interface UpdateListener {
   void onUpdateError(Update var1, Throwable var2);

   void onUpdateDownloading(Update var1);

   void onUpdateDownloadError(Update var1, Throwable var2);

   void onUpdateReady(Update var1);

   void onUpdateApplying(Update var1);

   void onUpdateApplyError(Update var1, Throwable var2);
}
