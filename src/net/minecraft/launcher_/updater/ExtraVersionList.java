package net.minecraft.launcher_.updater;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import net.minecraft.launcher_.Http;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.VersionSource;

public class ExtraVersionList extends VersionList {
   public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem) {
      return true;
   }

   protected String getUrl(String uri) throws IOException {
      String url = this.getRepositoryType().getDownloadPath() + Http.encode(uri);
      return Http.performGet(new URL(url), Proxy.NO_PROXY);
   }

   public VersionSource getRepositoryType() {
      return VersionSource.EXTRA;
   }
}
