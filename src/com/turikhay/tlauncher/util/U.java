package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.TLauncher;
import java.text.NumberFormat;
import java.util.Random;

public class U {
   private static TLauncher t;

   public static void linelog(Object what) {
      System.out.print(what);
   }

   public static void log(Object what) {
      System.out.print("[TLauncher] ");
      System.out.println(what);
   }

   public static void log(Object what0, Object what1) {
      System.out.print("[TLauncher] ");
      System.out.print(what0 + " ");
      System.out.println(what1);
   }

   public static void log(Object what, Throwable e) {
      log(what);
      log(e.toString());
      e.printStackTrace();
   }

   public static void plog(Object what) {
      System.out.println(what);
   }

   public static void plog(Object what0, Object what1) {
      System.out.println(what0 + " " + what1);
   }

   public static void setWorkingTo(TLauncher to) {
      if (t == null) {
         t = to;
      }

      MinecraftUtil.setWorkingTo(to);
   }

   public static double doubleRandom() {
      return (new Random(System.currentTimeMillis())).nextDouble();
   }

   public static int random(int s, int e) {
      return (new Random(System.currentTimeMillis())).nextInt(e - s) + s;
   }

   public static boolean ok(int d) {
      return (new Random(System.currentTimeMillis())).nextInt(d) == 0;
   }

   public static double getAverage(double[] d, int max) {
      double a = 0.0D;
      int k = 0;
      double[] var9 = d;
      int var8 = d.length;

      for(int var7 = 0; var7 < var8; ++var7) {
         double curd = var9[var7];
         a += curd;
         ++k;
         if (k == max) {
            break;
         }
      }

      return k == 0 ? 0.0D : a / (double)k;
   }

   public static int getAverage(int[] d, int max) {
      int a = 0;
      int k = 0;
      int[] var7 = d;
      int var6 = d.length;

      for(int var5 = 0; var5 < var6; ++var5) {
         int curd = var7[var5];
         a += curd;
         ++k;
         if (k == max) {
            break;
         }
      }

      return k == 0 ? 0 : Math.round((float)(a / k));
   }

   public static int getSum(int[] d) {
      int a = 0;
      int[] var5 = d;
      int var4 = d.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         int curd = var5[var3];
         a += curd;
      }

      return a;
   }

   public static double getSum(double[] d) {
      double a = 0.0D;
      double[] var7 = d;
      int var6 = d.length;

      for(int var5 = 0; var5 < var6; ++var5) {
         double curd = var7[var5];
         a += curd;
      }

      return a;
   }

   public static int getMaxMultiply(int i, int max) {
      if (i <= max) {
         return 1;
      } else {
         for(int x = max; x > 1; --x) {
            if (i % x == 0) {
               return x;
            }
         }

         return (int)Math.ceil((double)(i / max));
      }
   }

   public static String r(String string, int max) {
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

   public static String w(String string, int normal, char newline, boolean rude) {
      char[] c = string.toCharArray();
      int len = c.length;
      int remaining = normal;
      String ret = "";

      for(int x = 0; x < len; ++x) {
         --remaining;
         char cur = c[x];
         if (c[x] == newline) {
            remaining = normal;
         }

         if (remaining < 1 && cur == ' ') {
            remaining = normal;
            ret = ret + newline;
         } else {
            ret = ret + cur;
            if (remaining <= 0 && rude) {
               remaining = normal;
               ret = ret + newline;
            }
         }
      }

      return ret;
   }

   public static String w(String string, int max) {
      return w(string, max, '\n', false);
   }

   public static String setFractional(double d, int fractional) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(fractional);
      return nf.format(d).replace(",", ".");
   }

   public static String stackTrace(Throwable e) {
      String t = e.toString();
      if (t == null) {
         t = "";
      }

      StackTraceElement[] elems = e.getStackTrace();

      for(int x = 0; x < elems.length; ++x) {
         t = t + "\nat " + elems[x].toString();
         if (x >= 5) {
            t = t + "\n... and " + (elems.length - x - 1) + " more";
            break;
         }
      }

      Throwable cause = e.getCause();
      if (cause != null) {
         t = t + "\nCaused by: " + cause.toString();
         StackTraceElement[] causeelems = cause.getStackTrace();

         for(int x = 0; x < causeelems.length; ++x) {
            t = t + "\nat " + causeelems[x].toString();
            if (x >= 5) {
               t = t + "\n... and " + (causeelems.length - x - 1) + " more";
               break;
            }
         }
      }

      return t;
   }
}
