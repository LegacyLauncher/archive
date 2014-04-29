package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Iterator;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

public class RepositoryBasedVersionList extends RemoteVersionList {
   private final Repository repository;

   RepositoryBasedVersionList(Repository repository) {
      if (repository == null) {
         throw new NullPointerException();
      } else {
         this.repository = repository;
      }
   }

   public VersionList.RawVersionList getRawList() throws IOException {
      VersionList.RawVersionList rawList = super.getRawList();
      Iterator var3 = rawList.getVersions().iterator();

      while(var3.hasNext()) {
         Version version = (Version)var3.next();
         version.setSource(this.repository);
      }

      return rawList;
   }

   public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
      CompleteVersion complete = super.getCompleteVersion(version);
      complete.setSource(this.repository);
      return complete;
   }

   public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OS paramOperatingSystem) {
      return true;
   }

   protected String getUrl(String uri) throws IOException {
      return this.repository.getUrl(uri);
   }
}
