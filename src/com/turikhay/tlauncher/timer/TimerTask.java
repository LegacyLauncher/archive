package com.turikhay.tlauncher.timer;

public interface TimerTask extends Runnable {
   boolean isRepeating();

   int getTicks();
}
