package com.turikhay.util.logger;

import com.turikhay.util.stream.SafeOutputStream;

public class StringStream extends SafeOutputStream {
   final StringBuffer buffer = new StringBuffer();
   int caret;

   StringStream() {
   }

   public void write(int b) {
      this.write((char)b);
   }

   void write(char c) {
      this.buffer.append(c);
      ++this.caret;
   }

   public void write(char[] c) {
      if (c == null) {
         throw new NullPointerException();
      } else if (c.length != 0) {
         for(int i = 0; i < c.length; ++i) {
            this.write(c[i]);
         }

      }
   }

   public String getOutput() {
      return this.buffer.toString();
   }

   public int getLength() {
      return this.buffer.length();
   }
}