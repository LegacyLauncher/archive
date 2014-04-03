package com.turikhay.util.logger;

public class BufferedStringStream extends StringStream {
   int caretFlush;

   public void write(char b) {
      super.write(b);
      if (b == '\n') {
         this.flush();
         this.caretFlush = this.caret;
      }
   }
}
