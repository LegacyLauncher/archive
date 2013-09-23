package net.minecraft.launcher_.updater;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import net.minecraft.launcher_.Http;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.VersionSource;

public class RemoteVersionList extends VersionList
{
  public boolean hasAllFiles(CompleteVersion version, OperatingSystem os)
  {
    return true;
  }

  protected String getUrl(String uri) throws IOException
  {
    return Http.performGet(new URL("https://s3.amazonaws.com/Minecraft.Download/" + uri), Proxy.NO_PROXY);
  }

  public VersionSource getRepositoryType() {
	return VersionSource.REMOTE;
  }
}
