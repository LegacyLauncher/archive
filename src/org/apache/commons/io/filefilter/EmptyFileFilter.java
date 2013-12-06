package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class EmptyFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 3631422087512832211L;
   public static final IOFileFilter EMPTY = new EmptyFileFilter();
   public static final IOFileFilter NOT_EMPTY;

   static {
      NOT_EMPTY = new NotFileFilter(EMPTY);
   }

   protected EmptyFileFilter() {
   }

   public boolean accept(File file) {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         return files == null || files.length == 0;
      } else {
         return file.length() == 0L;
      }
   }
}
