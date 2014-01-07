package com.turikhay.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
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

   public long current() {
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
      Calendar ca = GregorianCalendar.getInstance();
      ca.setTimeZone(TimeZone.getTimeZone("UTC"));
      ca.setTimeInMillis(unixtime);
      int[] toret = new int[]{ca.get(3) - 1, ca.get(7) - 5, ca.get(11), ca.get(12), ca.get(13)};
      return toret;
   }

   public static String presentDate(Calendar cal, String format) {
      int month = 0;
      switch(cal.get(2)) {
      case 0:
         month = 1;
         break;
      case 1:
         month = 2;
         break;
      case 2:
         month = 3;
         break;
      case 3:
         month = 4;
         break;
      case 4:
         month = 5;
         break;
      case 5:
         month = 6;
         break;
      case 6:
         month = 7;
         break;
      case 7:
         month = 8;
         break;
      case 8:
         month = 9;
         break;
      case 9:
         month = 10;
         break;
      case 10:
         month = 11;
         break;
      case 11:
         month = 12;
      }

      return format.replaceAll("day", zero(cal.get(5))).replaceAll("month", zero(month)).replaceAll("year", String.valueOf(cal.get(1))).replaceAll("hour", zero(cal.get(11))).replaceAll("minute", zero(cal.get(12))).replaceAll("second", zero(cal.get(13)));
   }

   public static String presentDate(long unix, String format) {
      Calendar p = GregorianCalendar.getInstance();
      p.setTimeInMillis(unix);
      return presentDate(p, format);
   }

   public static String presentDate(long unix) {
      Calendar p = GregorianCalendar.getInstance();
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
      Long l = (Long)timers.get(holder);
      if (l == null) {
         return 0L;
      } else {
         timers.remove(holder);
         return System.currentTimeMillis() - l;
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
