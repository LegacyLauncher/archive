package net.minecraft.launcher.updater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class FileBasedVersionList extends VersionList
{
  protected String getUrl(String uri)
    throws IOException
  {
    InputStream inputStream = getFileInputStream(uri);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder result = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (result.length() > 0) result.append("\n");
      result.append(line);
    }

    reader.close();

    return result.toString();
  }

  protected abstract InputStream getFileInputStream(String paramString)
    throws FileNotFoundException;
}
