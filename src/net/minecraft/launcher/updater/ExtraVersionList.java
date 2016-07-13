package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;

public class ExtraVersionList extends RepositoryBasedVersionList {
   public ExtraVersionList() {
      super(Repository.EXTRA_VERSION_REPO);
   }

   public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
      if (version instanceof CompleteVersion) {
         return (CompleteVersion)version;
      } else if (version == null) {
         throw new NullPointerException("Version cannot be NULL!");
      } else {
         CompleteVersion complete = (CompleteVersion)this.gson.fromJson((Reader)this.getUrl("versions/" + version.getID() + ".json"), (Class)CompleteVersion.class);
         complete.setID(version.getID());
         complete.setVersionList(this);
         complete.setSource(Repository.EXTRA_VERSION_REPO);
         Collections.replaceAll(this.versions, version, complete);
         return complete;
      }
   }
}
