package net.minecraft.launcher_.versions;

import com.turikhay.tlauncher.util.U;
import java.net.Proxy;
import java.net.URL;
import net.minecraft.launcher_.Http;

public enum VersionSource {
   LOCAL(new String[1]),
   REMOTE(new String[]{"https://s3.amazonaws.com/Minecraft.Download/"}),
   EXTRA(new String[]{"http://ru-minecraft.org/update/tlauncher/extra/", "http://dl.dropboxusercontent.com/u/6204017/minecraft/tlauncher/extra/"});

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
         U.log("Selecting relevant path for", this.toString() + "...");
         int i = 0;

         while(i < this.urls.length) {
            try {
               Http.performGet(new URL(this.urls[i] + "check.txt"), Proxy.NO_PROXY);
               this.selected = i;
               U.log("Success: relevant path for", this.toString(), "is", this.urls[i]);
               return;
            } catch (Exception var3) {
               U.log("Failed: repo is not reachable:", this.urls[i]);
               ++i;
            }
         }

         U.log("Cannot select relevant path for", this.toString(), "because all repos are unreachable.");
      }
   }
}
