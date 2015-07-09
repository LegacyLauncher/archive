package ru.turikhay.util.stream;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;
import ru.turikhay.util.U;

public class PrintLogger extends PrintStream implements Logger {
   private LinkedStringStream stream;
   private Logger mirror;
   private Formatter formatter;

   private PrintLogger(LinkedStringStream stream, Logger mirror) {
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
      this.println(s);
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

   public synchronized void write(int b) {
      this.stream.write(b);
   }

   public synchronized void write(byte[] buf, int off, int len) {
      this.stream.write(buf, off, len);
   }

   private synchronized void write(String s) {
      if (s == null) {
         s = "null";
      }

      this.stream.write(s);
   }

   public synchronized void print(char c) {
      this.stream.write(c);
   }

   public synchronized void print(char[] s) {
      this.stream.write(s);
   }

   public synchronized void print(boolean b) {
      this.write(b ? "true" : "false");
   }

   public synchronized void print(int i) {
      this.write(String.valueOf(i));
   }

   public synchronized void print(long l) {
      this.write(String.valueOf(l));
   }

   public synchronized void print(float f) {
      this.write(String.valueOf(f));
   }

   public synchronized void print(double d) {
      this.write(String.valueOf(d));
   }

   public synchronized void print(String s) {
      this.write(s);
   }

   public synchronized void print(Object obj) {
      this.write(String.valueOf(obj));
   }

   private void newLine() {
      this.write(10);
   }

   public synchronized void println() {
      this.newLine();
   }

   public synchronized void println(boolean x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(char x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(int x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(long x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(float x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(double x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(char[] x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(String x) {
      this.print(x);
      this.newLine();
   }

   public synchronized void println(Object x) {
      this.print(String.valueOf(x));
      this.newLine();
   }

   public synchronized PrintStream printf(String format, Object... args) {
      return this.format(format, args);
   }

   public synchronized PrintStream printf(Locale l, String format, Object... args) {
      return this.format(l, format, args);
   }

   public synchronized PrintStream format(String format, Object... args) {
      if (this.formatter == null || this.formatter.locale() != Locale.getDefault()) {
         this.formatter = new Formatter(this);
      }

      this.formatter.format(Locale.getDefault(), format, args);
      return this;
   }

   public synchronized PrintStream format(Locale l, String format, Object... args) {
      if (this.formatter == null || this.formatter.locale() != l) {
         this.formatter = new Formatter(this, l);
      }

      this.formatter.format(l, format, args);
      return this;
   }

   public synchronized PrintStream append(CharSequence csq) {
      if (csq == null) {
         this.print("null");
      } else {
         this.print(csq.toString());
      }

      return this;
   }

   public synchronized PrintStream append(CharSequence csq, int start, int end) {
      CharSequence cs = csq == null ? "null" : csq;
      this.write(((CharSequence)cs).subSequence(start, end).toString());
      return this;
   }

   public synchronized PrintStream append(char c) {
      this.print(c);
      return this;
   }
}
