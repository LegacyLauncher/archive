package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class FalseFileFilter implements IOFileFilter, Serializable {
   private static final long serialVersionUID = 6210271677940926200L;
   public static final IOFileFilter FALSE = new FalseFileFilter();
   public static final IOFileFilter INSTANCE;

   static {
      INSTANCE = FALSE;
   }

   protected FalseFileFilter() {
   }

   public boolean accept(File file) {
      return false;
   }

   public boolean accept(File dir, String name) {
      return false;
   }
}
