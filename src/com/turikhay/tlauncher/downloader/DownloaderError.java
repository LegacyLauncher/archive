package com.turikhay.tlauncher.downloader;

public class DownloaderError extends RuntimeException {
   private static final long serialVersionUID = 3468802480206284946L;
   private boolean serious;
   private int timeout;

   public DownloaderError(String message, boolean isSerious) {
      super(message);
      this.serious = isSerious;
   }

   public DownloaderError(String message, int recommended_timeout) {
      super(message);
      this.serious = false;
      this.timeout = recommended_timeout;
   }

   public boolean isSerious() {
      return this.serious;
   }

   public int getTimeout() {
      return this.timeout;
   }

   public boolean hasTimeout() {
      return this.timeout > 0;
   }
}
