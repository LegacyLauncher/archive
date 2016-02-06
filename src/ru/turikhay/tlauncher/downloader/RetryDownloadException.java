package ru.turikhay.tlauncher.downloader;

import java.io.IOException;

public class RetryDownloadException extends IOException {
   public RetryDownloadException(String message) {
      super(message);
   }

   public RetryDownloadException(String message, Throwable cause) {
      super(message, cause);
   }
}
