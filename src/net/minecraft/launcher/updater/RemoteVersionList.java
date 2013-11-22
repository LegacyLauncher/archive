package net.minecraft.launcher.updater;

import java.io.IOException;
import java.net.URL;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.VersionSource;

public class RemoteVersionList extends VersionList {
   public boolean hasAllFiles(CompleteVersion version, OperatingSystem os) {
      return true;
   }

   protected String getUrl(String uri) throws IOException {
      return Http.performGet(new URL(this.getRepositoryType().getDownloadPath() + uri));
   }

   public VersionSource getRepositoryType() {
      return VersionSource.REMOTE;
   }
}
