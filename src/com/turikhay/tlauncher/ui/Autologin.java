package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.timer.TimerTask;

public class Autologin implements TimerTask {
   private LoginForm lf;
   boolean cancelled;
   private boolean last;
   private int timeout;
   private int elapsed = -1;

   public Autologin(LoginForm lf, int timeout) {
      this.lf = lf;
      this.timeout = timeout;
   }

   public void run() {
      ++this.elapsed;
      if (!this.cancelled && this.elapsed <= this.timeout) {
         this.lf.setAutologinRemaining(this.timeout - this.elapsed);
         if (this.timeout == this.elapsed) {
            this.last = true;
         } else if (!this.last) {
            return;
         }

         this.lf.callAutoLogin();
      }
   }

   public boolean isRepeating() {
      return !this.last;
   }

   public int getTicks() {
      return 1;
   }
}
