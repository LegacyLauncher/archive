package ru.turikhay.util.async;

public abstract class AsyncObject extends ExtendedThread {
   private AsyncObjectContainer container;
   private boolean gotValue;
   private Object value;
   private AsyncObjectGotErrorException error;

   protected AsyncObject() {
      super("AsyncObject");
   }

   public final AsyncObjectContainer getContainer() {
      return this.container;
   }

   void setContainer(AsyncObjectContainer container) {
      this.container = container;
   }

   public void run() {
      try {
         this.value = this.execute();
      } catch (Throwable var2) {
         this.error = new AsyncObjectGotErrorException(this, var2);
      }

      this.gotValue = true;
      if (this.container != null) {
         this.container.release();
      }

   }

   public Object getValue() throws AsyncObjectNotReadyException, AsyncObjectGotErrorException {
      if (this.error != null) {
         throw this.error;
      } else if (!this.gotValue) {
         throw new AsyncObjectNotReadyException();
      } else {
         return this.value;
      }
   }

   protected abstract Object execute() throws AsyncObjectGotErrorException;
}
