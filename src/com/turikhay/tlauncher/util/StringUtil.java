package com.turikhay.tlauncher.util;

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
}
