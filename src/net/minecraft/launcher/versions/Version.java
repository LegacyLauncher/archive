package net.minecraft.launcher.versions;

import com.turikhay.tlauncher.repository.Repository;
import java.util.Date;
import net.minecraft.launcher.updater.VersionList;

public interface Version {
   String getID();

   void setID(String var1);

   ReleaseType getReleaseType();

   Repository getSource();

   void setSource(Repository var1);

   Date getUpdatedTime();

   Date getReleaseTime();

   VersionList getVersionList();

   void setVersionList(VersionList var1);
}
