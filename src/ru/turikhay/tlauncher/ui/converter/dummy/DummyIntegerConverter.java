package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyIntegerConverter extends DummyConverter {
   public Integer fromDummyString(String from) throws RuntimeException {
      return Integer.parseInt(from);
   }

   public String toDummyValue(Integer value) throws RuntimeException {
      return value.toString();
   }

   public Class getObjectClass() {
      return Integer.class;
   }
}
