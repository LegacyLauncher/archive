package ru.turikhay.tlauncher.updater;

public interface UpdaterListener {
   void onUpdaterErrored(Updater.SearchFailed var1);

   void onUpdaterSucceeded(Updater.SearchSucceeded var1);
}
