package org.apache.commons.lang3;

public class Validate {
   public static CharSequence notBlank(CharSequence chars, String message, Object[] values) {
      if (chars == null) {
         throw new NullPointerException(String.format(message, values));
      } else if (StringUtils.isBlank(chars)) {
         throw new IllegalArgumentException(String.format(message, values));
      } else {
         return chars;
      }
   }

   public static CharSequence notBlank(CharSequence chars) {
      return notBlank(chars, "The validated character sequence is blank", new Object[0]);
   }
}
