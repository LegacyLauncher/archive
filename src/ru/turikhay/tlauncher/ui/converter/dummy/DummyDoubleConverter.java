package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyDoubleConverter extends DummyConverter {
   public Double fromDummyString(String from) throws RuntimeException {
      return Double.parseDouble(from);
   }

   public String toDummyValue(Double value) throws RuntimeException {
      return value.toString();
   }

   public Class getObjectClass() {
      return Double.class;
   }
}
