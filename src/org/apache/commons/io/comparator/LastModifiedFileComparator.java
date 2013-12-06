package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class LastModifiedFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = 7372168004395734046L;
   public static final Comparator LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();
   public static final Comparator LASTMODIFIED_REVERSE;

   static {
      LASTMODIFIED_REVERSE = new ReverseComparator(LASTMODIFIED_COMPARATOR);
   }

   public int compare(File file1, File file2) {
      long result = file1.lastModified() - file2.lastModified();
      if (result < 0L) {
         return -1;
      } else {
         return result > 0L ? 1 : 0;
      }
   }
}
