package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ArchiveDownloadable extends Downloadable {
   protected File folder;
   private final ArchiveDownloadable instance = this;
   protected DownloadableHandler handler = new DownloadableHandler() {
      public void onStart() {
      }

      public void onCompleteError() {
      }

      public void onComplete() {
         try {
            ArchiveDownloadable.this.unpack();
         } catch (IOException var2) {
            ArchiveDownloadable.this.instance.onError();
         }

      }
   };

   public ArchiveDownloadable(URL url, File folder, boolean force) {
      super((URL)url, createTemp(folder), (File[])null, force);
      this.init(folder);
   }

   public ArchiveDownloadable(String url, File folder, boolean force) throws MalformedURLException {
      super(url, createTemp(folder), force);
      this.init(folder);
   }

   private void init(File folder) {
      if (folder == null) {
         throw new NullPointerException("Folder is NULL!");
      } else {
         this.folder = folder;
         this.addHandler(this.handler);
      }
   }

   protected static File createTemp(File folder) {
      File parent = folder.getParentFile();
      if (parent == null) {
         parent = folder;
      }

      File ret = new File(parent, System.currentTimeMillis() + ".tlauncher.unzip");
      ret.deleteOnExit();
      return ret;
   }

   protected void unpack() throws IOException {
      FileUtil.unZip(this.getDestination(), this.folder, this.isForced());
      this.getDestination().delete();
   }
}
