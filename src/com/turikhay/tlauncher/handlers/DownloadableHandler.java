package com.turikhay.tlauncher.handlers;

public interface DownloadableHandler {
   void onStart();

   void onCompleteError();

   void onComplete();
}
