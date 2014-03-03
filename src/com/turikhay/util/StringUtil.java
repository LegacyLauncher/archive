package com.turikhay.util;

import com.turikhay.tlauncher.exceptions.ParseException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class StringUtil {
   public static String addQuotes(String a, char quote) {
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
         StringBuffer s = new StringBuffer(str);

         for(int i = 0; i < s.length(); ++i) {
            char[] var7;
            int var6 = (var7 = group.getChars()).length;

            for(int var5 = 0; var5 < var6; ++var5) {
               char c = var7[var5];
               if (s.charAt(i) == c) {
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

   public static enum EscapeGroup {
      COMMAND(new char[]{'\'', '"', ' '}),
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

         for(int i = 0; i < symbols.length; ++i) {
            this.chars[i + x] = symbols[i];
         }

      }

      public char[] getChars() {
         return this.chars;
      }
   }
}
