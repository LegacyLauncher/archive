package net.minecraft.launcher.versions;

import com.turikhay.tlauncher.util.U;
import java.net.URL;
import net.minecraft.launcher.Http;

public enum VersionSource {
   LOCAL(new String[1]),
   REMOTE(new String[]{"http://s3.amazonaws.com/Minecraft.Download/"}),
   EXTRA(new String[]{"http://5.9.120.11/update/versions/", "http://ru-minecraft.org/update/tlauncher/extra/", "http://dl.dropboxusercontent.com/u/6204017/update/versions/"});

   public static final int DEFAULT_TIMEOUT = 10000;
   private String[] urls;
   private int selected;

   private VersionSource(String[] url) {
      this.urls = url;
   }

   public String getDownloadPath() {
      return this.urls[this.selected];
   }

   public void selectRelevantPath() {
      if (this.urls.length >= 2) {
         this.log("Selecting relevant path...");
         int i = 0;
         int attempt = 0;

         while(i < 3) {
            ++i;
            int timeout = 10000 * i;
            int x = 0;

            while(x < this.urls.length) {
               ++attempt;
               this.log("Attempt #" + attempt + "; timeout: " + timeout + " ms; url: " + this.urls[x]);

               try {
                  Http.performGet(new URL(this.urls[x] + "check.txt"), timeout, timeout);
                  this.selected = x;
                  this.log("Success: Reached the repo!");
                  return;
               } catch (Exception var6) {
                  this.log("Failed: Repo is not reachable!");
                  ++x;
               }
            }
         }

         this.log("Failed: All repos are unreachable.");
      }
   }

   private void log(Object... obj) {
      U.log("[VersionSource:" + this.toString() + "]", obj);
   }
}
