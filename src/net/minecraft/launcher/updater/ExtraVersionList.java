package net.minecraft.launcher.updater;

import java.io.IOException;
import java.net.URL;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.VersionSource;

public class ExtraVersionList extends VersionList {
   public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem) {
      return true;
   }

   protected String getUrl(String uri) throws IOException {
      String url = this.getRepositoryType().getDownloadPath() + Http.encode(uri);
      return Http.performGet(new URL(url));
   }

   public VersionSource getRepositoryType() {
      return VersionSource.EXTRA;
   }
}
