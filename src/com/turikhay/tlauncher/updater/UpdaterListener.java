package com.turikhay.tlauncher.updater;

public interface UpdaterListener {
   void onUpdaterRequesting(Updater var1);

   void onUpdaterRequestError(Updater var1);

   void onUpdateFound(Update var1);

   void onUpdaterNotFoundUpdate(Updater var1);

   void onAdFound(Updater var1, Ad var2);
}
