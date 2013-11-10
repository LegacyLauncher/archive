package com.turikhay.tlauncher.ui;

public interface LoginListener {
   boolean onLogin();

   void onLoginFailed();

   void onLoginSuccess();
}
