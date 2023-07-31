package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class CanReadFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 3179904805251622989L;
   public static final IOFileFilter CAN_READ = new CanReadFileFilter();
   public static final IOFileFilter CANNOT_READ;
   public static final IOFileFilter READ_ONLY;

   static {
      CANNOT_READ = new NotFileFilter(CAN_READ);
      READ_ONLY = new AndFileFilter(CAN_READ, CanWriteFileFilter.CANNOT_WRITE);
   }

   protected CanReadFileFilter() {
   }

   public boolean accept(File file) {
      return file.canRead();
   }
}
