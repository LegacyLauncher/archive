package net.minecraft.launcher;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Http {
   public static URL constantURL(String input) {
      try {
         return new URL(input);
      } catch (MalformedURLException var2) {
         throw new Error(var2);
      }
   }

   public static String encode(String s) {
      try {
         return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%3A", ":").replaceAll("\\%2F", "/").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
      } catch (UnsupportedEncodingException var2) {
         throw new RuntimeException("UTF-8 is not supported.", var2);
      }
   }
}
