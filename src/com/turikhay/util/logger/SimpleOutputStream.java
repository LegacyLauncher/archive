package com.turikhay.util.logger;

import java.io.OutputStream;

public class SimpleOutputStream extends OutputStream {
   protected StringBuffer buffer = new StringBuffer();

   public void write(int b) {
      this.buffer.append((char)b);
   }

   public String getOutput() {
      return this.buffer.toString();
   }

   public void clear() {
      this.buffer.delete(0, this.buffer.length());
   }
}
