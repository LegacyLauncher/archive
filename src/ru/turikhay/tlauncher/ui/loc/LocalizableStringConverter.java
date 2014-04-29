package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class LocalizableStringConverter implements StringConverter {
   private final String prefix;

   public LocalizableStringConverter(String prefix) {
      this.prefix = prefix;
   }

   public String toString(Object from) {
      return Localizable.get(this.getPath(from));
   }

   String getPath(Object from) {
      String prefix = this.getPrefix();
      if (prefix != null && !prefix.isEmpty()) {
         String path = this.toPath(from);
         return prefix + "." + path;
      } else {
         return this.toPath(from);
      }
   }

   String getPrefix() {
      return this.prefix;
   }

   protected abstract String toPath(Object var1);
}
