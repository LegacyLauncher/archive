package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SafeOutputStream extends OutputStream {
   public void write(byte[] b) {
      try {
         super.write(b);
      } catch (IOException var3) {
      }

   }

   public void write(byte[] b, int off, int len) {
      try {
         super.write(b, off, len);
      } catch (IOException var5) {
      }

   }

   public void flush() {
   }

   public void close() {
   }

   public abstract void write(int var1);
}
