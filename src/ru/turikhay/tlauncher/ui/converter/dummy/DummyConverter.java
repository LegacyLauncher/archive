package ru.turikhay.tlauncher.ui.converter.dummy;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class DummyConverter implements StringConverter {
   private static DummyConverter[] converters;

   public static DummyConverter[] getConverters() {
      if (converters == null) {
         converters = new DummyConverter[]{new DummyStringConverter(), new DummyIntegerConverter(), new DummyDoubleConverter(), new DummyLongConverter(), new DummyDateConverter()};
      }

      return converters;
   }

   public Object fromString(String from) {
      return this.fromDummyString(from);
   }

   public String toString(Object from) {
      return this.toValue(from);
   }

   public String toValue(Object from) {
      return this.toDummyValue(from);
   }

   public abstract Object fromDummyString(String var1) throws RuntimeException;

   public abstract String toDummyValue(Object var1) throws RuntimeException;
}
