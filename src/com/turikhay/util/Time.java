package com.turikhay.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Time {
   private static final String defaultFormat = "[day.month hour:minute]";
   private static Map timers = new HashMap();
   private int offset;

   public Time(int rawOffset) {
      int timezoneOffset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
      this.offset = rawOffset - timezoneOffset;
   }

   public Time(TimeZone timezone) {
      this(timezone.getRawOffset());
   }

   long current() {
      return System.currentTimeMillis() + (long)this.offset;
   }

   public long seccurrent() {
      return (long)Math.round((float)(this.current() / 1000L));
   }

   public String presentDate(Calendar cal) {
      return presentDate(cal, "[day.month hour:minute]");
   }

   public String presentDate(String format) {
      return presentDate(this.current(), format);
   }

   public String presentDate() {
      return presentDate(this.current());
   }

   public int[] unix(long unixtime) {
      Calendar ca = Calendar.getInstance();
      ca.setTimeZone(TimeZone.getTimeZone("UTC"));
      ca.setTimeInMillis(unixtime);
      int[] toret = new int[]{ca.get(3) - 1, ca.get(7) - 5, ca.get(11), ca.get(12), ca.get(13)};
      return toret;
   }

   private static String presentDate(Calendar cal, String format) {
      return format.replaceAll("day", zero(cal.get(5))).replaceAll("month", zero(cal.get(2) + 1)).replaceAll("year", String.valueOf(cal.get(1))).replaceAll("hour", zero(cal.get(11))).replaceAll("minute", zero(cal.get(12))).replaceAll("second", zero(cal.get(13)));
   }

   private static String presentDate(long unix, String format) {
      Calendar p = Calendar.getInstance();
      p.setTimeInMillis(unix);
      return presentDate(p, format);
   }

   private static String presentDate(long unix) {
      Calendar p = Calendar.getInstance();
      p.setTimeInMillis(unix);
      return presentDate(p, "[day.month hour:minute]");
   }

   public static void start(Object holder) {
      if (timers.containsKey(holder)) {
         throw new IllegalArgumentException("This holder (" + holder.toString() + ") is already in use!");
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

   private static String zero(int integer) {
      return integer < 10 ? "0" + integer : String.valueOf(integer);
   }
}