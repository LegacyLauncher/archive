package ru.turikhay.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;
import org.apache.commons.io.IOUtils;

public class UrlEncoder {
   private static BitSet dontNeedEncoding = new BitSet(256);
   private final CharSequence s;

   public UrlEncoder(CharSequence sequence) {
      this.s = (CharSequence)U.requireNotNull(sequence);
   }

   public UrlEncoder.Encoder getEncoder() {
      return new UrlEncoder.Encoder();
   }

   public String encode() {
      try {
         return IOUtils.toString(new UrlEncoder.Encoder(), (String)"UTF-8");
      } catch (IOException var2) {
         throw new InternalError(var2);
      }
   }

   public static String encode(String str) {
      return (new UrlEncoder(str)).encode();
   }

   static {
      int i;
      for(i = 97; i <= 122; ++i) {
         dontNeedEncoding.set(i);
      }

      for(i = 65; i <= 90; ++i) {
         dontNeedEncoding.set(i);
      }

      for(i = 48; i <= 57; ++i) {
         dontNeedEncoding.set(i);
      }

      dontNeedEncoding.set(32);
      dontNeedEncoding.set(45);
      dontNeedEncoding.set(95);
      dontNeedEncoding.set(46);
      dontNeedEncoding.set(42);
   }

   public class Encoder extends InputStream {
      UrlEncoder.CharBuffer outputBuffer = UrlEncoder.this.new CharBuffer(4);
      UrlEncoder.CharBuffer surrogateBuffer = UrlEncoder.this.new CharBuffer(2);
      int i = -1;

      public int read() throws IOException {
         if (this.surrogateBuffer.hasFlush()) {
            String str = this.surrogateBuffer.toString();
            byte[] ba = str.getBytes("UTF-8");

            for(int j = 0; j < ba.length; ++j) {
               this.outputBuffer.append(37);
               char ch = Character.forDigit(ba[j] >> 4 & 15, 16);
               if (Character.isLetter(ch)) {
                  ch = (char)(ch - 32);
               }

               this.outputBuffer.append(ch);
               ch = Character.forDigit(ba[j] & 15, 16);
               if (Character.isLetter(ch)) {
                  ch = (char)(ch - 32);
               }

               this.outputBuffer.append(ch);
            }

            this.surrogateBuffer.reset();
         }

         if (this.outputBuffer.hasFlush()) {
            return this.outputBuffer.flush();
         } else if (++this.i >= UrlEncoder.this.s.length()) {
            return -1;
         } else {
            int c = UrlEncoder.this.s.charAt(this.i);
            if (UrlEncoder.dontNeedEncoding.get(c)) {
               if (c == ' ') {
                  c = '+';
               }

               return c;
            } else {
               do {
                  this.surrogateBuffer.append(c);
                  if (c >= '\ud800' && c <= '\udbff' && this.i + 1 < UrlEncoder.this.s.length()) {
                     int d = UrlEncoder.this.s.charAt(this.i + 1);
                     if (d >= '\udc00' && d <= '\udfff') {
                        this.surrogateBuffer.append(d);
                        ++this.i;
                     }
                  }

                  ++this.i;
               } while(this.i < UrlEncoder.this.s.length() && !UrlEncoder.dontNeedEncoding.get(c = UrlEncoder.this.s.charAt(this.i)));

               --this.i;
               return this.read();
            }
         }
      }
   }

   private class CharBuffer {
      private char[] buf;
      protected int start;
      protected int count;

      CharBuffer(int size) {
         this.buf = new char[size];
      }

      public void append(int c) {
         int newcount = this.count + 1;
         if (newcount > this.buf.length) {
            this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
         }

         this.buf[this.count] = (char)c;
         this.count = newcount;
      }

      public boolean hasFlush() {
         if (this.start < this.count) {
            return true;
         } else {
            this.reset();
            return false;
         }
      }

      public int flush() {
         return this.start == this.count ? -1 : this.buf[this.start++];
      }

      public void reset() {
         this.start = 0;
         this.count = 0;
      }

      public String toString() {
         return new String(this.buf, this.start, this.count);
      }
   }
}
