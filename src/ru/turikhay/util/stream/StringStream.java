package ru.turikhay.util.stream;

public class StringStream extends SafeOutputStream {
   protected final StringBuilder buffer = new StringBuilder();
   protected int caret;

   public void write(int b) {
      this.write((char)b);
   }

   protected synchronized void write(char c) {
      this.buffer.append(c);
      ++this.caret;
   }

   protected synchronized void write(CharSequence cs) {
      if (cs == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < cs.length(); ++i) {
            this.write(cs.charAt(i));
         }

      }
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

   public synchronized void flush() {
      this.caret = 0;
      this.buffer.setLength(0);
   }
}
