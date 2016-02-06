package ru.turikhay.util.stream;

public class BufferedOutputStringStream extends OutputStringStream {
   public void write(char b) {
      super.write(b);
      if (b == '\n') {
         this.flush();
      }

   }
}
