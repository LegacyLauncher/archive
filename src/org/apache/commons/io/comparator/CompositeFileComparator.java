package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CompositeFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = -2224170307287243428L;
   private static final Comparator[] NO_COMPARATORS = new Comparator[0];
   private final Comparator[] delegates;

   public CompositeFileComparator(Comparator... delegates) {
      if (delegates == null) {
         this.delegates = NO_COMPARATORS;
      } else {
         this.delegates = new Comparator[delegates.length];
         System.arraycopy(delegates, 0, this.delegates, 0, delegates.length);
      }

   }

   public CompositeFileComparator(Iterable delegates) {
      if (delegates == null) {
         this.delegates = NO_COMPARATORS;
      } else {
         List list = new ArrayList();
         Iterator var4 = delegates.iterator();

         while(var4.hasNext()) {
            Comparator comparator = (Comparator)var4.next();
            list.add(comparator);
         }

         this.delegates = (Comparator[])list.toArray(new Comparator[list.size()]);
      }

   }

   public int compare(File file1, File file2) {
      int result = 0;
      Comparator[] var7;
      int var6 = (var7 = this.delegates).length;

      for(int var5 = 0; var5 < var6; ++var5) {
         Comparator delegate = var7[var5];
         result = delegate.compare(file1, file2);
         if (result != 0) {
            break;
         }
      }

      return result;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(super.toString());
      builder.append('{');

      for(int i = 0; i < this.delegates.length; ++i) {
         if (i > 0) {
            builder.append(',');
         }

         builder.append(this.delegates[i]);
      }

      builder.append('}');
      return builder.toString();
   }
}
