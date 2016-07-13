package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;

public class AppenderRepo extends Repo {
   private final String prefix;
   private final String suffix;

   public AppenderRepo(String prefix, String suffix) {
      super(prefix);
      if (StringUtils.isEmpty(prefix)) {
         throw new IllegalArgumentException("prefix is empty");
      } else {
         if (StringUtils.isEmpty(suffix)) {
            suffix = null;
         }

         this.prefix = prefix;
         this.suffix = suffix;
      }
   }

   public AppenderRepo(String prefix) {
      this(prefix, (String)null);
   }

   protected URL makeUrl(String path) throws IOException {
      String url;
      if (this.suffix == null) {
         url = this.prefix + path;
      } else {
         url = this.prefix + path + this.suffix;
      }

      return new URL(url);
   }

   static AppenderRepo[] fromString(String... arr) {
      if (((String[])U.requireNotNull(arr)).length == 0) {
         return new AppenderRepo[0];
      } else {
         AppenderRepo[] result = new AppenderRepo[((String[])U.requireNotNull(arr)).length];

         for(int i = 0; i < arr.length; ++i) {
            result[i] = new AppenderRepo(arr[i]);
         }

         return result;
      }
   }
}
