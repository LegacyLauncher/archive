package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOCase;

public class NameFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 176844364689077340L;
   private final String[] names;
   private final IOCase caseSensitivity;

   public NameFileFilter(String name) {
      this((String)name, (IOCase)null);
   }

   public NameFileFilter(String name, IOCase caseSensitivity) {
      if (name == null) {
         throw new IllegalArgumentException("The wildcard must not be null");
      } else {
         this.names = new String[]{name};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public NameFileFilter(String[] names) {
      this((String[])names, (IOCase)null);
   }

   public NameFileFilter(String[] names, IOCase caseSensitivity) {
      if (names == null) {
         throw new IllegalArgumentException("The array of names must not be null");
      } else {
         this.names = new String[names.length];
         System.arraycopy(names, 0, this.names, 0, names.length);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public NameFileFilter(List names) {
      this((List)names, (IOCase)null);
   }

   public NameFileFilter(List names, IOCase caseSensitivity) {
      if (names == null) {
         throw new IllegalArgumentException("The list of names must not be null");
      } else {
         this.names = (String[])names.toArray(new String[names.size()]);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      String name = file.getName();
      String[] var6;
      int var5 = (var6 = this.names).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         String name2 = var6[var4];
         if (this.caseSensitivity.checkEquals(name, name2)) {
            return true;
         }
      }

      return false;
   }

   public boolean accept(File dir, String name) {
      String[] var6;
      int var5 = (var6 = this.names).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         String name2 = var6[var4];
         if (this.caseSensitivity.checkEquals(name, name2)) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.names != null) {
         for(int i = 0; i < this.names.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.names[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
