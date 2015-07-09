package ru.turikhay.util.stream;

public class BufferedStringStream extends StringStream {
   public void write(char b) {
      super.write(b);
      if (b == '\n') {
         this.flush();
      }

   }
}
