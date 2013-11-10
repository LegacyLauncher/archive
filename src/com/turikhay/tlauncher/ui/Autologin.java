package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.AsyncThread;

public class Autologin implements LoginListener {
   public static final int DEFAULT_TIMEOUT = 3;
   public static final int MAX_TIMEOUT = 10;
   private final LoginForm lf;
   public final int timeout;
   boolean enabled;
   boolean active;
   private Runnable task;
   private int sec;

   Autologin(LoginForm loginform, boolean enabled, int timeout) {
      if (timeout < 2) {
         timeout = 2;
      }

      this.lf = loginform;
      this.enabled = enabled;
      this.timeout = this.sec = timeout;
      this.task = new Runnable() {
         public void run() {
            while(Autologin.this.sec > 0) {
               Autologin.this.sleepFor(1000L);
               if (Autologin.this.updateLogin()) {
                  Autologin.this.callLogin();
               }
            }

         }
      };
   }

   public void startLogin() {
      this.active = true;
      AsyncThread.execute(this.task);
   }

   public void stopLogin() {
      this.sec = -1;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void cancel() {
      this.enabled = false;
      this.stopLogin();
      this.lf.checkbox.uncheckAutologin();
      this.lf.buttons.toggleSouthButton();
      if (this.active) {
         this.lf.versionchoice.asyncRefresh();
      }

   }

   private boolean updateLogin() {
      --this.sec;
      this.lf.buttons.cancel.setLabel("loginform.cancel", "t", this.sec);
      if (this.sec != 0) {
         return false;
      } else {
         this.stopLogin();
         return true;
      }
   }

   private void callLogin() {
      this.lf.callLogin();
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }

   public boolean onLogin() {
      if (!this.enabled) {
         return true;
      } else {
         this.stopLogin();
         this.active = false;
         this.lf.buttons.toggleSouthButton();
         return true;
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
