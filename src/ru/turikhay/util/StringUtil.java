package ru.turikhay.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Random;
import ru.turikhay.exceptions.ParseException;

public class StringUtil {
   private static String addQuotes(String a, char quote) {
      if (a == null) {
         return null;
      } else {
         return a.length() == 0 ? "" : quote + a.replaceAll("\\" + quote, "\\\\" + quote) + quote;
      }
   }

   public static String addQuotes(String a) {
      return addQuotes(a, '"');
   }

   public static String addSlashes(String str, StringUtil.EscapeGroup group) {
      if (str == null) {
         return "";
      } else {
         StringBuilder s = new StringBuilder(str);

         for(int i = 0; i < s.length(); ++i) {
            char curChar = s.charAt(i);
            char[] var8;
            int var7 = (var8 = group.getChars()).length;

            for(int var6 = 0; var6 < var7; ++var6) {
               char c = var8[var6];
               if (curChar == c) {
                  s.insert(i++, '\\');
               }
            }
         }

         return s.toString();
      }
   }

   public static String[] addSlashes(String[] str, StringUtil.EscapeGroup group) {
      if (str == null) {
         return null;
      } else {
         int len = str.length;
         String[] ret = new String[len];

         for(int i = 0; i < len; ++i) {
            ret[i] = addSlashes(str[i], group);
         }

         return ret;
      }
   }

   public static String iconv(String inChar, String outChar, String str) {
      Charset in = Charset.forName(inChar);
      Charset out = Charset.forName(outChar);
      CharsetDecoder decoder = in.newDecoder();
      CharsetEncoder encoder = out.newEncoder();

      try {
         ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(str));
         CharBuffer cbuf = decoder.decode(bbuf);
         return cbuf.toString();
      } catch (Exception var9) {
         var9.printStackTrace();
         return null;
      }
   }

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
         if (len == 0) {
            return '\u0000';
         } else {
            return len == 1 ? str.charAt(0) : str.charAt(len - 1);
         }
      }
   }

   public static String randomizeWord(String str, boolean softly) {
      if (str == null) {
         return null;
      } else {
         int len = str.length();
         if (len < 4) {
            return str;
         } else {
            boolean[] reversedFlag = new boolean[len];
            if (softly) {
               reversedFlag[0] = true;
            }

            boolean chosenLastLetter = !softly;
            char[] chars = str.toCharArray();

            int i;
            int tries;
            for(i = len - 1; i > -1; --i) {
               char curChar = chars[i];
               tries = Character.getType(curChar);
               boolean canBeReversed = tries == 1 || tries == 2;
               reversedFlag[i] |= !canBeReversed;
               if (canBeReversed && !chosenLastLetter) {
                  reversedFlag[i] = true;
                  chosenLastLetter = true;
               }
            }

            for(i = 0; i < len; ++i) {
               if (!reversedFlag[i]) {
                  int newPos = i;
                  tries = 0;

                  while(tries < 3) {
                     ++tries;
                     newPos = (new Random()).nextInt(len);
                     if (!reversedFlag[newPos]) {
                        tries = 10;
                        break;
                     }
                  }

                  if (tries == 10) {
                     char curChar = chars[i];
                     char replaceChar = chars[newPos];
                     chars[i] = replaceChar;
                     chars[newPos] = curChar;
                     reversedFlag[i] = true;
                     reversedFlag[newPos] = true;
                  }
               }
            }

            return new String(chars);
         }
      }
   }

   public static String randomizeWord(String str) {
      return randomizeWord(str, true);
   }

   public static String randomize(String str, boolean softly) {
      if (str == null) {
         return null;
      } else if (str.isEmpty()) {
         return str;
      } else {
         String[] lines = str.split("\n");
         StringBuilder lineBuilder = new StringBuilder();
         boolean isFirstLine = true;

         for(int l = 0; l < lines.length; ++l) {
            String line = lines[l];
            String[] words = line.split(" ");
            StringBuilder wordBuilder = new StringBuilder();
            boolean isFirstWord = true;

            for(int w = 0; w < words.length; ++w) {
               if (isFirstWord) {
                  isFirstWord = false;
               } else {
                  wordBuilder.append(' ');
               }

               wordBuilder.append(randomizeWord(words[w]));
            }

            if (isFirstLine) {
               isFirstLine = false;
            } else {
               lineBuilder.append('\n');
            }

            lineBuilder.append(wordBuilder);
         }

         return lineBuilder.toString();
      }
   }

   public static String randomize(String str) {
      return randomize(str, true);
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
               if (s[x] != '\n' && (remaining >= 1 || current != ' ')) {
                  if (lookForward(s, x, lineBreak)) {
                     remaining = maxChars;
                  }

                  builder.append(current);
                  if (remaining <= 0 && rudeBreaking) {
                     remaining = maxChars;
                     builder.append(lineBreak);
                  }
               } else {
                  remaining = maxChars;
                  builder.append(lineBreak);
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

   public static String wrap(String s, int maxChars, boolean rudeBreaking, boolean detectHTML) {
      return wrap(s.toCharArray(), maxChars, rudeBreaking, detectHTML);
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

   public static enum EscapeGroup {
      DOUBLE_QUOTE(new char[]{'"'}),
      COMMAND(DOUBLE_QUOTE, new char[]{'\'', ' '}),
      REGEXP(COMMAND, new char[]{'/', '\\', '?', '*', '+', '[', ']', ':', '{', '}', '(', ')'});

      private final char[] chars;

      private EscapeGroup(char... symbols) {
         this.chars = symbols;
      }

      private EscapeGroup(StringUtil.EscapeGroup extend, char... symbols) {
         int len = extend.chars.length + symbols.length;
         this.chars = new char[len];

         int x;
         for(x = 0; x < extend.chars.length; ++x) {
            this.chars[x] = extend.chars[x];
         }

         System.arraycopy(symbols, 0, this.chars, x, symbols.length);
      }

      public char[] getChars() {
         return this.chars;
      }
   }
}
