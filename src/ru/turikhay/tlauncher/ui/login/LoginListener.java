package ru.turikhay.tlauncher.ui.login;

public interface LoginListener {
   void onLogin() throws LoginException;

   void onLoginFailed();

   void onLoginSuccess();
}
