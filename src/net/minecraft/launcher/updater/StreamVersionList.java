package net.minecraft.launcher.updater;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class StreamVersionList extends VersionList {
   protected InputStreamReader getUrl(String uri) throws IOException {
      return new InputStreamReader(this.getInputStream(uri), "UTF-8");
   }

   protected abstract InputStream getInputStream(String var1) throws IOException;
}
