package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class CanWriteFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 5132005214688990379L;
   public static final IOFileFilter CAN_WRITE = new CanWriteFileFilter();
   public static final IOFileFilter CANNOT_WRITE;

   static {
      CANNOT_WRITE = new NotFileFilter(CAN_WRITE);
   }

   protected CanWriteFileFilter() {
   }

   public boolean accept(File file) {
      return file.canWrite();
   }
}
