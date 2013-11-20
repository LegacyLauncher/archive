package net.minecraft.launcher_.versions;

import com.turikhay.tlauncher.util.U;
import java.net.URL;
import net.minecraft.launcher_.Http;

public enum VersionSource {
   LOCAL(new String[1]),
   REMOTE(new String[]{"http://s3.amazonaws.com/Minecraft.Download/"}),
   EXTRA(new String[]{"http://5.9.120.11/upd/versions/", "http://ru-minecraft.org/update/tlauncher/extra/", "http://dl.dropboxusercontent.com/u/6204017/update/versions/"});

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
         int attempt = 0;

         while(attempt < 2) {
            ++attempt;
            int timeout = 5000 * attempt;
            int i = 0;

            while(i < this.urls.length) {
               this.log("Attempt #" + attempt + "; timeout: " + timeout + " ms; url: " + this.urls[i]);

               try {
                  Http.performGet(new URL(this.urls[i] + "check.txt"), timeout, timeout);
                  this.selected = i;
                  this.log("Success: Reached the repo!");
                  return;
               } catch (Exception var5) {
                  this.log("Failed: Repo is not reachable!");
                  ++i;
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
