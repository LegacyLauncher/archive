package ru.turikhay.util.stream;

public interface Logger {
   void log(String var1);

   void log(Object... var1);

   void rawlog(String var1);

   void rawlog(char[] var1);
}
