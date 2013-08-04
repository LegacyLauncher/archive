package com.turikhay.tlauncher.downloader;

public interface DownloadableHandler {
   void onStart();

   void onCompleteError();

   void onComplete();
}
