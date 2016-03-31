package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.InputStream;

public class DelegatorInputStream extends InputStream {
   private final InputStream delegated;

   public DelegatorInputStream(InputStream delegated) {
      this.delegated = delegated;
   }

   public int read() throws IOException {
      return this.delegated.read();
   }

   public int read(byte[] b) throws IOException {
      return this.delegated.read(b);
   }

   public int read(byte[] b, int off, int len) throws IOException {
      return this.delegated.read(b, off, len);
   }

   public long skip(long n) throws IOException {
      return this.delegated.skip(n);
   }

   public int available() throws IOException {
      return this.delegated.available();
   }

   public void close() throws IOException {
      this.delegated.close();
   }

   public synchronized void mark(int readlimit) {
      this.delegated.mark(readlimit);
   }

   public synchronized void reset() throws IOException {
      this.delegated.reset();
   }

   public boolean markSupported() {
      return this.delegated.markSupported();
   }
}
