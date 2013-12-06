package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class HiddenFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 8930842316112759062L;
   public static final IOFileFilter HIDDEN = new HiddenFileFilter();
   public static final IOFileFilter VISIBLE;

   static {
      VISIBLE = new NotFileFilter(HIDDEN);
   }

   protected HiddenFileFilter() {
   }

   public boolean accept(File file) {
      return file.isHidden();
   }
}
