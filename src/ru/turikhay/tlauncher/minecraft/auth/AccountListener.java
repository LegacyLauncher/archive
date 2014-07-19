package ru.turikhay.tlauncher.minecraft.auth;

public interface AccountListener {
	public void onAccountsRefreshed(AuthenticatorDatabase db);

}
