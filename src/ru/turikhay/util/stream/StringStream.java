package ru.turikhay.util.stream;

public class StringStream extends SafeOutputStream {
   protected final StringBuffer buffer = new StringBuffer();
   protected int caret;

   public void write(int b) {
      this.write((char)b);
   }

   protected void write(char c) {
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

   public char getCharAt(int i) {
      return this.buffer.charAt(i);
   }

   public String getOutput() {
      return this.buffer.toString();
   }

   public int getLength() {
      return this.buffer.length();
   }
}
