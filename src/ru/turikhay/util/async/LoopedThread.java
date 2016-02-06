package ru.turikhay.util.async;

public abstract class LoopedThread extends ExtendedThread {
   public LoopedThread(String name) {
      super(name);
   }

   public LoopedThread() {
      this("LoopedThread");
   }

   protected final void lockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else if (!reason.equals("iteration")) {
         throw new IllegalArgumentException("Illegal block reason. Expected: iteration, got: " + reason);
      } else {
         super.lockThread(reason);
      }
   }

   public final boolean isIterating() {
      return !this.isThreadLocked();
   }

   public void iterate() {
      if (!this.isIterating()) {
         this.unlockThread("iteration");
      }

   }

   public final void run() {
      while(true) {
         this.lockThread("iteration");
         this.iterateOnce();
      }
   }

   protected abstract void iterateOnce();
}
