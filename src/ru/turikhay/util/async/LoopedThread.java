package ru.turikhay.util.async;

public abstract class LoopedThread extends ExtendedThread {
   protected static final String LOOPED_BLOCK = "iteration";

   public LoopedThread(String name) {
      super(name);
   }

   public LoopedThread() {
      this("LoopedThread");
   }

   protected final synchronized void blockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else if (!reason.equals("iteration")) {
         throw new IllegalArgumentException("Illegal block reason. Expected: iteration, got: " + reason);
      } else {
         super.blockThread(reason);
      }
   }

   public final boolean isIterating() {
      return !this.isThreadBlocked();
   }

   public final void iterate() {
      if (!this.isIterating()) {
         this.unblockThread(new String[]{"iteration"});
      }

   }

   public final void run() {
      while(true) {
         this.blockThread("iteration");
         this.iterateOnce();
      }
   }

   protected abstract void iterateOnce();
}
