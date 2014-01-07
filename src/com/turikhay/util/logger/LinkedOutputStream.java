package com.turikhay.util.logger;

public class LinkedOutputStream extends SimpleOutputStream {
   private Logger logger;
   private boolean newLine;

   public void write(int b) {
      super.write(b);
      if (this.logger != null) {
         if (this.newLine) {
            this.logger.rawlog(10);
            this.newLine = false;
         }

         if ((char)b == '\n') {
            this.newLine = true;
         } else {
            this.logger.rawlog(b);
         }
      }
   }

   public Logger getLogger() {
      return this.logger;
   }

   public void setLogger(Logger logger) {
      if (logger == null) {
         throw new NullPointerException();
      } else {
         this.logger = logger;
      }
   }
}
