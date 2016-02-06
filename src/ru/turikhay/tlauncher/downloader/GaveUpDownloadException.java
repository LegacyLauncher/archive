package ru.turikhay.tlauncher.downloader;

import java.io.IOException;

class GaveUpDownloadException extends IOException {
   GaveUpDownloadException(Downloadable d, Throwable cause) {
      super(d.getURL(), cause);
   }
}
