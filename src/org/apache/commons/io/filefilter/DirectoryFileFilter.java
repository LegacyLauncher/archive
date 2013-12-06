package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class DirectoryFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -5148237843784525732L;
   public static final IOFileFilter DIRECTORY = new DirectoryFileFilter();
   public static final IOFileFilter INSTANCE;

   static {
      INSTANCE = DIRECTORY;
   }

   protected DirectoryFileFilter() {
   }

   public boolean accept(File file) {
      return file.isDirectory();
   }
}
