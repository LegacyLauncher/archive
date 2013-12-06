package com.turikhay.tlauncher.ui;

public interface AnimatedBackground {
   void start();

   void stop();

   void suspend();

   boolean isAllowed();

   void setAllowed(boolean var1);
}
