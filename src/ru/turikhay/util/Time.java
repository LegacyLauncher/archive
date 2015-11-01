package ru.turikhay.util;

import java.util.Hashtable;
import java.util.Map;

public class Time {
   private static Map timers = new Hashtable();

   public static void start(Object holder) {
      if (timers.containsKey(holder)) {
         throw new IllegalArgumentException("This holder (" + holder + ") is already in use!");
      } else {
         timers.put(holder, System.currentTimeMillis());
      }
   }

   public static long stop(Object holder) {
      long current = System.currentTimeMillis();
      Long l = (Long)timers.get(holder);
      if (l == null) {
         return 0L;
      } else {
         timers.remove(holder);
         return current - l;
      }
   }

   public static void start() {
      start(Thread.currentThread());
   }

   public static long stop() {
      return stop(Thread.currentThread());
   }
}
