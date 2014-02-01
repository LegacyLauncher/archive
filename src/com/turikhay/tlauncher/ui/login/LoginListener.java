package com.turikhay.tlauncher.ui.login;

public interface LoginListener {
   boolean onLogin();

   void onLoginFailed();

   void onLoginSuccess();
}
