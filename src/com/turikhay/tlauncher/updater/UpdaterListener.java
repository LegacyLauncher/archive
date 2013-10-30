package com.turikhay.tlauncher.updater;

public interface UpdaterListener {
   void onUpdaterRequesting(Updater var1);

   void onUpdaterRequestError(Updater var1);

   void onUpdateFound(Updater var1, Update var2);

   void onUpdaterNotFoundUpdate(Updater var1);

   void onAdFound(Updater var1, Ad var2);
}
