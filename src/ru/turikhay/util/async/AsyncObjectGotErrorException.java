package ru.turikhay.util.async;

public class AsyncObjectGotErrorException extends AsyncObjectException {
   private final AsyncObject object;

   public AsyncObjectGotErrorException(AsyncObject object, Throwable error) {
      super(error);
      this.object = object;
   }
}
