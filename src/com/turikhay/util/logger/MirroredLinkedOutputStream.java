package com.turikhay.util.logger;

import java.io.IOException;
import java.io.OutputStream;

public class MirroredLinkedOutputStream extends LinkedOutputStream {
   private OutputStream mirror;

   public void write(int b) {
      super.write(b);
      if (this.mirror != null) {
         try {
            this.mirror.write(b);
         } catch (IOException var3) {
            throw new RuntimeException("Cannot write into mirror stream!", var3);
         }
      }
   }

   public OutputStream getMirror() {
      return this.mirror;
   }

   public void setMirror(OutputStream mirror) {
      if (mirror == null) {
         throw new NullPointerException();
      } else {
         this.mirror = mirror;
      }
   }
}
