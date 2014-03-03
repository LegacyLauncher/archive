package com.turikhay.util.logger;

public class LinkedStringStream extends BufferedStringStream {
   private Logger logger;

   public LinkedStringStream() {
   }

   public LinkedStringStream(Logger logger) {
      this.logger = logger;
   }

   public Logger getLogger() {
      return this.logger;
   }

   public void setLogger(Logger logger) {
      this.logger = logger;
   }

   public void flush() {
      if (this.logger != null) {
         char[] chars = new char[this.caret - this.caretFlush];
         this.buffer.getChars(this.caretFlush, this.caret, chars, 0);
         this.logger.rawlog(chars);
      }
   }
}
