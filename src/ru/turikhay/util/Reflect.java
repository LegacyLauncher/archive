package ru.turikhay.util;

import ru.turikhay.exceptions.ParseException;

public class Reflect {
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
}
