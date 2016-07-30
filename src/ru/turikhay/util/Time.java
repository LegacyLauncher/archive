package ru.turikhay.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public final class Time {
   private static final ArrayList holders = new ArrayList();

   public static Object start(Object object) {
      synchronized(holders) {
         Iterator i = holders.iterator();

         while(i.hasNext()) {
            Time.TimeHolder holder = (Time.TimeHolder)i.next();
            switch(holder.isHolding(object)) {
            case TRUE:
               throw new IllegalStateException("object is already being held");
            case UNDEFINED:
               i.remove();
            }
         }

         holders.add(new Time.TimeHolder(object));
         return object;
      }
   }

   private static long stop(Object object, long currentTime) {
      synchronized(holders) {
         Iterator i = holders.iterator();

         Time.TimeHolder holder;
         do {
            if (!i.hasNext()) {
               throw new IllegalStateException("object is not being held");
            }

            holder = (Time.TimeHolder)i.next();
         } while(holder.isHolding(object) != Time.NotBoolean.TRUE);

         long delta = currentTime - holder.timestamp;
         i.remove();
         return delta;
      }
   }

   public static long stop(Object object) {
      return stop(object, System.currentTimeMillis());
   }

   public static long[] stop(Object... object) {
      long currentTime = System.currentTimeMillis();
      long[] deltas = new long[object.length];
      synchronized(holders) {
         for(int i = 0; i < object.length; ++i) {
            deltas[i] = stop(object[i], currentTime);
         }

         return deltas;
      }
   }

   public static void start() {
      start(Thread.currentThread());
   }

   public static long stop() {
      return stop((Object)Thread.currentThread());
   }

   static enum NotBoolean {
      FALSE,
      TRUE,
      UNDEFINED;
   }

   private static class TimeHolder {
      private final WeakReference ref;
      private final long timestamp;

      TimeHolder(Object o) {
         if (o == null) {
            throw new NullPointerException();
         } else {
            this.ref = new WeakReference(o);
            this.timestamp = System.currentTimeMillis();
         }
      }

      Time.NotBoolean isHolding(Object object) {
         Object o = this.ref.get();
         if (o == null) {
            return Time.NotBoolean.UNDEFINED;
         } else {
            return o == object ? Time.NotBoolean.TRUE : Time.NotBoolean.FALSE;
         }
      }
   }
}
