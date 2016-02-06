package net.minecraft.launcher.versions;

import java.util.Date;
import net.minecraft.launcher.updater.VersionList;
import ru.turikhay.tlauncher.repository.Repository;

public interface Version {
   String getID();

   ReleaseType getReleaseType();

   Repository getSource();

   void setSource(Repository var1);

   Date getUpdatedTime();

   Date getReleaseTime();

   VersionList getVersionList();

   void setVersionList(VersionList var1);
}
