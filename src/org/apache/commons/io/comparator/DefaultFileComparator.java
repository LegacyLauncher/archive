package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class DefaultFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = 3260141861365313518L;
   public static final Comparator DEFAULT_COMPARATOR = new DefaultFileComparator();
   public static final Comparator DEFAULT_REVERSE;

   static {
      DEFAULT_REVERSE = new ReverseComparator(DEFAULT_COMPARATOR);
   }

   public int compare(File file1, File file2) {
      return file1.compareTo(file2);
   }
}
