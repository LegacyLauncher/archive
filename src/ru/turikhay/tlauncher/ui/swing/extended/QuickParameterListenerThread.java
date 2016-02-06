package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class QuickParameterListenerThread extends LoopedThread {
   private final IntegerArrayGetter paramGetter;
   private final Runnable runnable;
   private final int tick;

   QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run, int tick) {
      super("QuickParameterListenerThread");
      if (getter == null) {
         throw new NullPointerException("Getter is NULL!");
      } else if (run == null) {
         throw new NullPointerException("Runnable is NULL!");
      } else if (tick < 0) {
         throw new IllegalArgumentException("Tick must be positive!");
      } else {
         this.paramGetter = getter;
         this.runnable = run;
         this.tick = tick;
         this.setPriority(1);
         this.startAndWait();
      }
   }

   void startListening() {
      this.iterate();
   }

   protected void iterateOnce() {
      int[] initial = this.paramGetter.getIntegerArray();
      boolean var2 = false;

      boolean equal;
      do {
         this.sleep();
         int[] newvalue = this.paramGetter.getIntegerArray();
         equal = true;

         for(int var5 = 0; var5 < initial.length; ++var5) {
            if (initial[var5] != newvalue[var5]) {
               equal = false;
            }
         }

         initial = newvalue;
      } while(!equal);

      this.runnable.run();
   }

   private void sleep() {
      U.sleepFor((long)this.tick);
   }
}
