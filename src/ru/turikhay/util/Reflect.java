package ru.turikhay.util;

import java.lang.reflect.Field;
import ru.turikhay.exceptions.ParseException;

public class Reflect {
   public static Field getField0(Class clazz, String name) throws NoSuchFieldException, SecurityException {
      if (clazz == null) {
         throw new NullPointerException("class is null");
      } else if (name != null && !name.isEmpty()) {
         return clazz.getField(name);
      } else {
         throw new NullPointerException("name is null or empty");
      }
   }

   public static Field getField(Class clazz, String name) {
      try {
         return getField0(clazz, name);
      } catch (Exception var3) {
         U.log("Error getting field", name, "from", clazz, var3);
         return null;
      }
   }

   public static Object getValue0(Field field, Class classOfT, Object parent) throws IllegalArgumentException, IllegalAccessException {
      if (field == null) {
         throw new NullPointerException("field is null");
      } else if (classOfT == null) {
         throw new NullPointerException("classOfT is null");
      } else if (parent == null) {
         throw new NullPointerException("parent is NULL");
      } else {
         Class fieldClass = field.getType();
         if (fieldClass == null) {
            throw new NullPointerException("field has no shell");
         } else if (!fieldClass.equals(classOfT) && !fieldClass.isAssignableFrom(classOfT)) {
            throw new IllegalArgumentException("field is not assignable from return type class");
         } else {
            return field.get(parent);
         }
      }
   }

   public static Object getValue(Field field, Class classOfT, Object parent) {
      try {
         return getValue0(field, classOfT, parent);
      } catch (Exception var4) {
         U.log("Cannot get value of", field, "from", classOfT, parent, var4);
         return null;
      }
   }

   public static Object cast(Object o, Class classOfT) {
      if (classOfT == null) {
         throw new NullPointerException();
      } else {
         return classOfT.isInstance(o) ? classOfT.cast(o) : null;
      }
   }

   public static Enum parseEnum0(Class enumClass, String string) throws ParseException {
      if (enumClass == null) {
         throw new NullPointerException("class is null");
      } else if (string == null) {
         throw new NullPointerException("string is null");
      } else {
         Enum[] constants = (Enum[])enumClass.getEnumConstants();
         Enum[] arr$ = constants;
         int len$ = constants.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enum constant = arr$[i$];
            if (string.equalsIgnoreCase(constant.toString())) {
               return constant;
            }
         }

         throw new ParseException("Cannot parse value:\"" + string + "\"; enum: " + enumClass.getSimpleName());
      }
   }

   public static Enum parseEnum(Class enumClass, String string) {
      try {
         return parseEnum0(enumClass, string);
      } catch (Exception var3) {
         U.log(var3);
         return null;
      }
   }

   private Reflect() {
      throw new RuntimeException();
   }
}
