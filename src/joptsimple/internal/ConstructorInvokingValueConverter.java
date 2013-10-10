package joptsimple.internal;

import java.lang.reflect.Constructor;
import joptsimple.ValueConverter;

class ConstructorInvokingValueConverter implements ValueConverter {
   private final Constructor ctor;

   ConstructorInvokingValueConverter(Constructor ctor) {
      this.ctor = ctor;
   }

   public Object convert(String value) {
      return Reflection.instantiate(this.ctor, value);
   }

   public Class valueType() {
      return this.ctor.getDeclaringClass();
   }

   public String valuePattern() {
      return null;
   }
}
