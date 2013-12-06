package com.turikhay.tlauncher.downloader;

import com.turikhay.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ArchiveDownloadable extends Downloadable {
   protected File folder;

   public ArchiveDownloadable(URL url, File folder, boolean force) {
      super((URL)url, createTemp(folder), (File[])null, force);
      this.init(folder);
   }

   public ArchiveDownloadable(String url, File folder, boolean force) throws MalformedURLException {
      super(url, createTemp(folder), force);
      this.init(folder);
   }

   public void onComplete() {
      try {
         FileUtil.unZip(this.getDestination(), this.folder, this.isForced());
         if (!this.getDestination().delete()) {
            throw new IOException("Cannot remove temp file!");
         }
      } catch (IOException var2) {
         this.error = var2;
         this.onError();
         return;
      }

      super.onComplete();
   }

   private void init(File folder) {
      if (folder == null) {
         throw new NullPointerException("Folder is NULL!");
      } else {
         this.folder = folder;
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
}
