package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyLongConverter extends DummyConverter {
   public Long fromDummyString(String from) throws RuntimeException {
      return Long.parseLong(from);
   }

   public String toDummyValue(Long value) throws RuntimeException {
      return value.toString();
   }

   public Class getObjectClass() {
      return Long.class;
   }
}
