package com.turikhay.tlauncher.ui.login;

public interface LoginListener {
	public void onLogin() throws LoginException;
	public void onLoginFailed();
	public void onLoginSuccess();
}
