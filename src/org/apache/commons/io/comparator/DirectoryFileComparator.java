package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class DirectoryFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = 296132640160964395L;
   public static final Comparator DIRECTORY_COMPARATOR = new DirectoryFileComparator();
   public static final Comparator DIRECTORY_REVERSE;

   static {
      DIRECTORY_REVERSE = new ReverseComparator(DIRECTORY_COMPARATOR);
   }

   public int compare(File file1, File file2) {
      return this.getType(file1) - this.getType(file2);
   }

   private int getType(File file) {
      return file.isDirectory() ? 1 : 2;
   }
}
