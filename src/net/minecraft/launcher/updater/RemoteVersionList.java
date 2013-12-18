package net.minecraft.launcher.updater;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.VersionSource;

public class RemoteVersionList extends VersionList
{
  public boolean hasAllFiles(CompleteVersion version, OperatingSystem os)
  {
    return true;
  }

  public VersionSource getRepositoryType() {
	return VersionSource.REMOTE;
  }
}
