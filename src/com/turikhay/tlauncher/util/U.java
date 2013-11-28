package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.TLauncher;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Random;

public class U {
   public static final int DEFAULT_READ_TIMEOUT = 15000;
   public static final int DEFAULT_CONNECTION_TIMEOUT = 15000;
   private static final String PREFIX = "[TLauncher]";
   private static boolean conBusy = false;

   public static void linelog(Object what) {
      while(conBusy) {
         sleepFor(10L);
      }

      conBusy = true;
      System.out.print(what);
      conBusy = false;
   }

   public static void log(Object... what) {
      hlog("[TLauncher]", what);
   }

   public static void plog(Object... what) {
      hlog((String)null, what);
   }

   private static void hlog(String prefix, Object[] append) {
      while(conBusy) {
         sleepFor(10L);
      }

      conBusy = true;
      System.out.println(toLog(prefix, append));
      conBusy = false;
   }

   public static String toLog(String prefix, Object... append) {
      StringBuilder b = new StringBuilder();
      boolean first = true;
      if (prefix != null) {
         b.append(prefix);
         first = false;
      }

      if (append != null) {
         Object[] var7 = append;
         int var6 = append.length;

         for(int var5 = 0; var5 < var6; ++var5) {
            Object e = var7[var5];
            if (e != null && e.getClass().isArray()) {
               if (!first) {
                  b.append(" ");
               }

               if (e instanceof Object[]) {
                  b.append(toLog((Object[])e));
               } else {
                  b.append(arrayToLog(e));
               }
            } else if (e instanceof Throwable) {
               if (!first) {
                  b.append("\n");
               }

               b.append(stackTrace((Throwable)e));
               b.append("\n");
            } else {
               if (!first) {
                  b.append(" ");
               }

               b.append(e);
               if (first) {
                  first = false;
               }
            }
         }
      } else {
         b.append("null");
      }

      return b.toString();
   }

   public static String toLog(Object... append) {
      return toLog((String)null, append);
   }

   public static String arrayToLog(Object e) {
      if (!e.getClass().isArray()) {
         throw new IllegalArgumentException("Given object is not an array!");
      } else {
         StringBuilder b = new StringBuilder();
         boolean first = true;
         int var4;
         int var5;
         if (e instanceof Object[]) {
            Object[] var6;
            var5 = (var6 = (Object[])e).length;

            for(var4 = 0; var4 < var5; ++var4) {
               Object i = var6[var4];
               if (!first) {
                  b.append(" ");
               } else {
                  first = false;
               }

               b.append(i);
            }
         } else if (e instanceof int[]) {
            int[] var16;
            var5 = (var16 = (int[])e).length;

            for(var4 = 0; var4 < var5; ++var4) {
               int i = var16[var4];
               if (!first) {
                  b.append(" ");
               } else {
                  first = false;
               }

               b.append(i);
            }
         } else if (e instanceof boolean[]) {
            boolean[] var17;
            var5 = (var17 = (boolean[])e).length;

            for(var4 = 0; var4 < var5; ++var4) {
               boolean i = var17[var4];
               if (!first) {
                  b.append(" ");
               } else {
                  first = false;
               }

               b.append(i);
            }
         } else {
            int var18;
            if (e instanceof long[]) {
               long[] var7;
               var18 = (var7 = (long[])e).length;

               for(var5 = 0; var5 < var18; ++var5) {
                  long i = var7[var5];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            } else if (e instanceof float[]) {
               float[] var19;
               var5 = (var19 = (float[])e).length;

               for(var4 = 0; var4 < var5; ++var4) {
                  float i = var19[var4];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            } else if (e instanceof double[]) {
               double[] var20;
               var18 = (var20 = (double[])e).length;

               for(var5 = 0; var5 < var18; ++var5) {
                  double i = var20[var5];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            } else if (e instanceof byte[]) {
               byte[] var21;
               var5 = (var21 = (byte[])e).length;

               for(var4 = 0; var4 < var5; ++var4) {
                  byte i = var21[var4];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            } else if (e instanceof short[]) {
               short[] var22;
               var5 = (var22 = (short[])e).length;

               for(var4 = 0; var4 < var5; ++var4) {
                  short i = var22[var4];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            } else if (e instanceof char[]) {
               char[] var23;
               var5 = (var23 = (char[])e).length;

               for(var4 = 0; var4 < var5; ++var4) {
                  char i = var23[var4];
                  if (!first) {
                     b.append(" ");
                  } else {
                     first = false;
                  }

                  b.append(i);
               }
            }
         }

         if (b.length() == 0) {
            throw new UnknownError("Unknown array type given.");
         } else {
            return b.toString();
         }
      }
   }

   public static short shortRandom() {
      return (short)(new Random(System.currentTimeMillis())).nextInt(32767);
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

   public static double getAverage(double[] d) {
      double a = 0.0D;
      int k = 0;
      double[] var8 = d;
      int var7 = d.length;

      for(int var6 = 0; var6 < var7; ++var6) {
         double curd = var8[var6];
         a += curd;
         ++k;
         if (a == 0.0D) {
            break;
         }
      }

      return k == 0 ? 0.0D : a / (double)k;
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

   public static String t(String string, int max) {
      if (string == null) {
         return null;
      } else {
         int len = string.length();
         return len <= max ? string : string.substring(0, max) + "...";
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
      if (e == null) {
         return null;
      } else {
         String t = e.toString();
         if (t == null) {
            t = "";
         }

         StackTraceElement[] elems = e.getStackTrace();
         boolean found_out = false;

         for(int x = 0; x < elems.length; ++x) {
            String elem = elems[x].toString();
            t = t + "\nat " + elem;
            if (!found_out) {
               found_out = elem.startsWith("com.turikhay");
            }

            if (x >= 5 && found_out) {
               int remain = elems.length - x - 1;
               if (remain != 0) {
                  t = t + "\n... and " + remain + " more";
               }
               break;
            }
         }

         Throwable cause = e.getCause();
         if (cause != null) {
            t = t + "\nCaused by: " + stackTrace(cause);
         }

         return t;
      }
   }

   public static long getUsingSpace() {
      return getTotalSpace() - getFreeSpace();
   }

   public static long getFreeSpace() {
      return Runtime.getRuntime().freeMemory() / 1048576L;
   }

   public static long getTotalSpace() {
      return Runtime.getRuntime().totalMemory() / 1048576L;
   }

   public static void gc() {
      log("Starting garbage collector: " + getUsingSpace() + " / " + getTotalSpace() + " MB");
      System.gc();
      log("Garbage collector completed: " + getUsingSpace() + " / " + getTotalSpace() + " MB");
   }

   public static void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public static URL makeURL(String p) {
      try {
         return new URL(p);
      } catch (Exception var2) {
         return null;
      }
   }

   public static URI makeURI(URL url) {
      try {
         return url.toURI();
      } catch (Exception var2) {
         return null;
      }
   }

   public static URI makeURI(String p) {
      return makeURI(makeURL(p));
   }

   public static boolean interval(int min, int max, int num, boolean including) {
      return including ? num >= min && num <= max : num > max && num < max;
   }

   public static boolean interval(int min, int max, int num) {
      return interval(min, max, num, true);
   }

   public static long m() {
      return System.currentTimeMillis();
   }

   public static long n() {
      return System.nanoTime();
   }

   public static int getReadTimeout() {
      return getConnectionTimeout();
   }

   public static int getConnectionTimeout() {
      TLauncher t = TLauncher.getInstance();
      if (t == null) {
         return 15000;
      } else {
         int timeout = t.getSettings().getInteger("timeout.connection");
         return timeout < 1 ? 15000 : timeout;
      }
   }
}
