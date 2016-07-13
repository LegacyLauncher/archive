package net.minecraft.launcher.updater;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import net.minecraft.launcher.versions.PartialVersion;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

public class OfficialVersionList extends RemoteVersionList {
   public VersionList.RawVersionList getRawList() throws IOException {
      Object lock = new Object();
      Time.start(lock);
      VersionList.RawVersionList list = (VersionList.RawVersionList)this.gson.fromJson((Reader)this.getUrl("version_manifest.json"), (Class)VersionList.RawVersionList.class);
      Iterator var4 = list.versions.iterator();

      while(var4.hasNext()) {
         PartialVersion version = (PartialVersion)var4.next();
         version.setVersionList(this);
      }

      this.log(new Object[]{"Got in", Time.stop(lock), "ms"});
      return list;
   }

   protected InputStreamReader getUrl(String var1) throws IOException {
      return new InputStreamReader((new URL("https://launchermeta.mojang.com/mc/game/" + var1)).openConnection(U.getProxy()).getInputStream(), "UTF-8");
   }
}
