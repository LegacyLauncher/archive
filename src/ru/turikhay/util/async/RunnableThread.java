package ru.turikhay.util.async;

public class RunnableThread extends ExtendedThread {
   private final Runnable r;

   public RunnableThread(Runnable r) {
      if (r == null) {
         throw new NullPointerException();
      } else {
         this.r = r;
      }
   }

   public void run() {
      this.r.run();
   }
}
