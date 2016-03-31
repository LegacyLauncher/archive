package ru.turikhay.util;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.exceptions.ParseException;

public class StringUtil {
   public static boolean parseBoolean(String b) throws ParseException {
      if (b == null) {
         throw new ParseException("String cannot be NULL!");
      } else if (b.equalsIgnoreCase("true")) {
         return true;
      } else if (b.equalsIgnoreCase("false")) {
         return false;
      } else {
         throw new ParseException("Cannot parse value (" + b + ")!");
      }
   }

   public static int countLines(String str) {
      if (str != null && str.length() != 0) {
         int lines = 1;
         int len = str.length();

         for(int pos = 0; pos < len; ++pos) {
            char c = str.charAt(pos);
            if (c == '\r') {
               ++lines;
               if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                  ++pos;
               }
            } else if (c == '\n') {
               ++lines;
            }
         }

         return lines;
      } else {
         return 0;
      }
   }

   public static char lastChar(String str) {
      if (str == null) {
         throw new NullPointerException();
      } else {
         int len = str.length();
         return len == 0 ? '\u0000' : (len == 1 ? str.charAt(0) : str.charAt(len - 1));
      }
   }

   public static boolean isHTML(char[] s) {
      if (s != null && s.length >= 6 && s[0] == '<' && s[5] == '>') {
         String tag = new String(s, 1, 4);
         return tag.equalsIgnoreCase("html");
      } else {
         return false;
      }
   }

   public static String wrap(char[] s, int maxChars, boolean rudeBreaking, boolean detectHTML) {
      if (s == null) {
         throw new NullPointerException("sequence");
      } else if (maxChars < 1) {
         throw new IllegalArgumentException("maxChars < 1");
      } else {
         detectHTML = detectHTML && isHTML(s);
         String lineBreak = detectHTML ? "<br />" : "\n";
         StringBuilder builder = new StringBuilder();
         int len = s.length;
         int remaining = maxChars;
         boolean tagDetecting = false;
         boolean ignoreCurrent = false;

         for(int x = 0; x < len; ++x) {
            char current = s[x];
            if (current == '<' && detectHTML) {
               tagDetecting = true;
               ignoreCurrent = true;
            } else if (tagDetecting) {
               if (current == '>') {
                  tagDetecting = false;
               }

               ignoreCurrent = true;
            }

            if (ignoreCurrent) {
               ignoreCurrent = false;
               builder.append(current);
            } else {
               --remaining;
               if (s[x] == '\n' || remaining < 1 && current == ' ') {
                  remaining = maxChars;
                  builder.append(lineBreak);
               } else {
                  if (lookForward(s, x, lineBreak)) {
                     remaining = maxChars;
                  }

                  builder.append(current);
                  if (remaining <= 0 && rudeBreaking) {
                     remaining = maxChars;
                     builder.append(lineBreak);
                  }
               }
            }
         }

         return builder.toString();
      }
   }

   private static boolean lookForward(char[] c, int caret, CharSequence search) {
      if (c == null) {
         throw new NullPointerException("char array");
      } else if (caret < 0) {
         throw new IllegalArgumentException("caret < 0");
      } else if (caret >= c.length) {
         return false;
      } else {
         int length = search.length();
         int available = c.length - caret;
         if (length < available) {
            return false;
         } else {
            for(int i = 0; i < length; ++i) {
               if (c[caret + i] != search.charAt(i)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static String wrap(char[] s, int maxChars, boolean rudeBreaking) {
      return wrap(s, maxChars, rudeBreaking, true);
   }

   public static String wrap(char[] s, int maxChars) {
      return wrap(s, maxChars, false);
   }

   public static String wrap(String s, int maxChars) {
      return wrap(s.toCharArray(), maxChars);
   }

   public static String cut(String string, int max) {
      if (string == null) {
         return null;
      } else {
         int len = string.length();
         if (len <= max) {
            return string;
         } else {
            String[] words = string.split(" ");
            String ret = "";
            int remaining = max + 1;

            for(int x = 0; x < words.length; ++x) {
               String curword = words[x];
               int curlen = curword.length();
               if (curlen >= remaining) {
                  if (x == 0) {
                     ret = ret + " " + curword.substring(0, remaining - 1);
                  }
                  break;
               }

               ret = ret + " " + curword;
               remaining -= curlen + 1;
            }

            return ret.length() == 0 ? "" : ret.substring(1) + "...";
         }
      }
   }

   public static String requireNotBlank(String s, String name) {
      if (s == null) {
         throw new NullPointerException(name);
      } else if (StringUtils.isBlank(s)) {
         throw new IllegalArgumentException(name);
      } else {
         return s;
      }
   }
}
