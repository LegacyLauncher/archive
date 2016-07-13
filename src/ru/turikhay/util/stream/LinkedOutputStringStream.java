package ru.turikhay.util.stream;

public class LinkedOutputStringStream extends BufferedOutputStringStream {
   private StreamLogger logger;

   public void setLogger(StreamLogger logger) {
      this.logger = logger;
   }

   public synchronized void flush() {
      if (this.logger != null) {
         char[] chars = new char[this.caret];
         this.buffer.getChars(0, this.caret, chars, 0);
         this.logger.rawlog(chars);
      }

      super.flush();
   }
}
