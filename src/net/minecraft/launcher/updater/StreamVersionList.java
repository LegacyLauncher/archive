package net.minecraft.launcher.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class StreamVersionList extends VersionList {
   protected String getUrl(String uri) throws IOException {
      InputStream inputStream = this.getInputStream(uri);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

      StringBuilder result;
      String line;
      for(result = new StringBuilder(); (line = reader.readLine()) != null; result.append(line)) {
         if (result.length() > 0) {
            result.append("\n");
         }
      }

      reader.close();
      return result.toString();
   }

   protected abstract InputStream getInputStream(String var1) throws IOException;
}
