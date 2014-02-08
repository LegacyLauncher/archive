package com.turikhay.tlauncher.ui.progress;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.ui.loc.LocalizableProgressBar;
import java.awt.Component;

public class DownloaderProgress extends LocalizableProgressBar {
   private static final long serialVersionUID = -8382205925341380876L;
   private final DownloaderProgress instance;
   private DownloadListener listener;

   public DownloaderProgress(Component parentComp, Downloader downloader) {
      super(parentComp);
      if (downloader == null) {
         throw new NullPointerException();
      } else {
         this.instance = this;
         this.listener = new DownloadListener() {
            public void onDownloaderStart(Downloader d, int files) {
               DownloaderProgress.this.instance.startProgress();
               DownloaderProgress.this.setIndeterminate(true);
               DownloaderProgress.this.setCenterString("progressBar.init");
               DownloaderProgress.this.setEastString("progressBar.downloading", new Object[]{files});
            }

            public void onDownloaderAbort(Downloader d) {
               DownloaderProgress.this.instance.stopProgress();
            }

            public void onDownloaderComplete(Downloader d) {
               DownloaderProgress.this.instance.stopProgress();
            }

            public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
            }

            public void onDownloaderProgress(Downloader d, int progress, double speed) {
               if (progress > 0) {
                  if (DownloaderProgress.this.getValue() > progress) {
                     return;
                  }

                  DownloaderProgress.this.setIndeterminate(false);
                  DownloaderProgress.this.setValue(progress);
                  DownloaderProgress.this.setCenterString(progress + "%");
               }

            }

            public void onDownloaderFileComplete(Downloader d, Downloadable file) {
               DownloaderProgress.this.setIndeterminate(false);
               DownloaderProgress.this.setWestString("progressBar.completed", new Object[]{file.getFilename()});
               DownloaderProgress.this.setEastString("progressBar.remaining", new Object[]{d.getRemaining()});
            }
         };
         downloader.addListener(this.listener);
         this.stopProgress();
      }
   }

   public DownloaderProgress(Component parentComp) {
      this(parentComp, TLauncher.getInstance().getDownloader());
   }
}
