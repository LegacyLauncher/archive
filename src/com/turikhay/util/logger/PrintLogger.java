package com.turikhay.util.logger;

import com.turikhay.util.U;
import java.io.PrintStream;

public class PrintLogger extends PrintStream implements Logger {
   private LinkedStringStream stream;
   private Logger mirror;

   public PrintLogger(LinkedStringStream stream, Logger mirror) {
      super(stream);
      this.stream = stream;
      this.mirror = mirror;
   }

   public PrintLogger(LinkedStringStream stream) {
      this(stream, (Logger)null);
   }

   public Logger getMirror() {
      return this.mirror;
   }

   public void setMirror(Logger logger) {
      if (this == logger) {
         throw new IllegalArgumentException();
      } else {
         this.mirror = logger;
      }
   }

   public void log(Object... o) {
      this.log(U.toLog(o));
   }

   public void log(String s) {
      this.print(s);
      this.print('\n');
      if (this.mirror != null) {
         this.mirror.log(s);
      }

   }

   public void rawlog(String s) {
      if (this.mirror != null) {
         this.mirror.rawlog(s);
      }

   }

   public void rawlog(char[] c) {
      if (this.mirror != null) {
         this.mirror.rawlog(c);
      }

   }

   public LinkedStringStream getStream() {
      return this.stream;
   }
}
