package ru.turikhay.util.stream;

import java.io.InputStream;

public class InputStringStream extends InputStream {
   private final String s;
   private int i = -1;

   public InputStringStream(String s) {
      if (s == null) {
         throw new NullPointerException();
      } else {
         this.s = s;
      }
   }

   public int available() {
      return this.i == -1 ? this.s.length() : this.s.length() - this.i - 1;
   }

   public int read() {
      return ++this.i >= this.s.length() ? -1 : this.s.charAt(this.i);
   }
}
