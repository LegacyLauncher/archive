package net.minecraft.launcher.updater;

import ru.turikhay.util.U;

public class AssetIndexInfo extends DownloadInfo {
   protected String id;
   protected boolean known = true;

   public AssetIndexInfo() {
   }

   public AssetIndexInfo(String id) {
      this.id = id;
      this.url = U.makeURL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + id + ".json");
      this.known = false;
   }

   public String getId() {
      return this.id;
   }
}
