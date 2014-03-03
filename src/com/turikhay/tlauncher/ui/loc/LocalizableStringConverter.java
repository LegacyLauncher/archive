package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class LocalizableStringConverter implements StringConverter {
   private final String prefix;

   public LocalizableStringConverter(String prefix) {
      this.prefix = prefix;
   }

   public String toString(Object from) {
      return Localizable.get(this.getPath(from));
   }

   public String getPath(Object from) {
      String prefix = this.getPrefix();
      if (prefix != null && !prefix.isEmpty()) {
         String path = this.toPath(from);
         return prefix + "." + path;
      } else {
         return this.toPath(from);
      }
   }

   public String getPrefix() {
      return this.prefix;
   }

   public abstract String toPath(Object var1);
}
