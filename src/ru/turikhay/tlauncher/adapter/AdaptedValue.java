package ru.turikhay.tlauncher.adapter;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public class AdaptedValue {
   private final Object object;
   private final String key;
   private Object value;
   private final StringConverter converter;

   public AdaptedValue(Object object, String key, Object value, StringConverter converter) {
      if (object == null) {
         throw new NullPointerException("Object is NULL!");
      } else if (key == null) {
         throw new NullPointerException("Key is NULL!");
      } else if (converter == null) {
         throw new NullPointerException("Converter is NULL!");
      } else {
         this.object = object;
         this.key = key;
         this.value = value;
         this.converter = converter;
      }
   }

   public Object getObject() {
      return this.object;
   }

   public String getKey() {
      return this.key;
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   public String getStringValue() {
      return this.converter.toValue(this.value);
   }

   public StringConverter getConverter() {
      return this.converter;
   }
}
