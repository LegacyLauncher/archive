package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.OutputStream;

public class MirroredLinkedOutputStringStream extends LinkedOutputStringStream {
   private OutputStream mirror;

   public OutputStream getMirror() {
      return this.mirror;
   }

   public void setMirror(OutputStream stream) {
      this.mirror = stream;
   }

   public void write(char b) {
      super.write(b);
      if (this.mirror != null) {
         try {
            this.mirror.write(b);
         } catch (IOException var3) {
            throw new RuntimeException("Cannot log into the mirror!", var3);
         }
      }

   }

   public void flush() {
      super.flush();
      if (this.mirror != null) {
         try {
            this.mirror.flush();
         } catch (IOException var2) {
            throw new RuntimeException("Cannot flush the mirror!", var2);
         }
      }

   }
}
