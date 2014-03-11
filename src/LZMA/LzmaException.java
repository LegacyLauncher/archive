package LZMA;

import java.io.IOException;

class LzmaException extends IOException {
   private static final long serialVersionUID = 3689351022372206390L;

   public LzmaException() {
   }

   public LzmaException(String msg) {
      super(msg);
   }
}
