package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.exceptions.ParseException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.List;

public class StringUtil {
   public static String addQuotes(List a, String quotes, String del) {
      if (a == null) {
         return null;
      } else if (a.size() == 0) {
         return "";
      } else {
         String t = "";

         String cs;
         for(Iterator var5 = a.iterator(); var5.hasNext(); t = t + del + quotes + cs + quotes) {
            cs = (String)var5.next();
         }

         return t.substring(del.length());
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

   public static boolean parseBoolean(String b, boolean ignoreCase) throws ParseException {
      if (b == null) {
         throw new ParseException("String cannot be NULL!");
      } else {
         if (ignoreCase) {
            b = b.toLowerCase();
         }

         switch(b.hashCode()) {
         case 3569038:
            if (b.equals("true")) {
               return true;
            }
            break;
         case 97196323:
            if (b.equals("false")) {
               return false;
            }
         }

         throw new ParseException("Cannot parse value (" + b + ")!");
      }
   }

   public static boolean parseBoolean(String b) throws ParseException {
      return parseBoolean(b, true);
   }
}
