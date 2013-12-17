package net.minecraft.launcher.updater;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.VersionSource;

public class ExtraVersionList extends VersionList {
   public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem) {
      return true;
   }

   public VersionSource getRepositoryType() {
      return VersionSource.EXTRA;
   }
}
