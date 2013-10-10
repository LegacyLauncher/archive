package joptsimple.internal;

public class Objects {
   public static void ensureNotNull(Object target) {
      if (target == null) {
         throw new NullPointerException();
      }
   }
}
