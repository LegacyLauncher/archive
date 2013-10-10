package org.apache.commons.lang3;

public class StringUtils {
   public static boolean isEmpty(CharSequence cs) {
      return cs == null || cs.length() == 0;
   }

   public static boolean isBlank(CharSequence cs) {
      int strLen;
      if (cs != null && (strLen = cs.length()) != 0) {
         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(cs.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean isNotBlank(CharSequence cs) {
      return !isBlank(cs);
   }

   public static String repeat(char ch, int repeat) {
      char[] buf = new char[repeat];

      for(int i = repeat - 1; i >= 0; --i) {
         buf[i] = ch;
      }

      return new String(buf);
   }
}
