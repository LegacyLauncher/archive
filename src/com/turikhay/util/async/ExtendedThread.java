package com.turikhay.util.async;

public class ExtendedThread extends Thread {
   private static int threadNum;
   private final ExtendedThread.ExtendedThreadCaller caller = new ExtendedThread.ExtendedThreadCaller();

   public ExtendedThread() {
      super("ExtendedThread#" + threadNum++);
   }

   public ExtendedThread(String name) {
      super(name);
   }

   public ExtendedThread.ExtendedThreadCaller getCaller() {
      return this.caller;
   }

   public class ExtendedThreadCaller extends RuntimeException {
      private static final long serialVersionUID = -9184403765829112550L;
   }
}
