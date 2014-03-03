package com.turikhay.tlauncher.ui.progress;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.downloader.DownloaderListener;
import com.turikhay.tlauncher.ui.loc.LocalizableProgressBar;
import java.awt.Component;

public class DownloaderProgress extends LocalizableProgressBar {
   private static final long serialVersionUID = -8382205925341380876L;
   private final DownloaderProgress instance;
   private DownloaderListener listener;

   public DownloaderProgress(Component parentComp, Downloader downloader) {
      super(parentComp);
      if (downloader == null) {
         throw new NullPointerException();
      } else {
         this.instance = this;
         this.listener = new DownloaderListener() {
            public void onDownloaderStart(Downloader d, int files) {
               DownloaderProgress.this.instance.startProgress();
               DownloaderProgress.this.setIndeterminate(true);
               DownloaderProgress.this.setCenterString("progressBar.init");
               DownloaderProgress.this.setEastString("progressBar.downloading", new Object[]{files});
            }

            public void onDownloaderAbort(Downloader d) {
               DownloaderProgress.this.instance.stopProgress();
            }

            public void onDownloaderProgress(Downloader d, double dprogress, double speed) {
               if (dprogress > 0.0D) {
                  int progress = (int)(dprogress * 100.0D);
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
               DownloaderProgress.this.setEastString("progressBar.remaining", new Object[]{d.getRemaining()});
            }

            public void onDownloaderComplete(Downloader d) {
               DownloaderProgress.this.instance.stopProgress();
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
