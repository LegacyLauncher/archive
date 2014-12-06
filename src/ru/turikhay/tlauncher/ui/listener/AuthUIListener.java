package ru.turikhay.tlauncher.ui.listener;

import java.io.IOException;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorException;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;

public class AuthUIListener implements AuthenticatorListener {
	private final AuthenticatorListener listener;

	private final boolean showErrorOnce;
	private boolean errorShown;

	public AuthUIListener(boolean showErrorOnce, AuthenticatorListener listener) {
		this.listener = listener;
		this.showErrorOnce = showErrorOnce;
	}

	@Override
	public void onAuthPassing(Authenticator auth) {
		if (listener == null)
			return;
		listener.onAuthPassing(auth);
	}

	@Override
	public void onAuthPassingError(Authenticator auth, Throwable e) {
		this.showError(e);

		if (listener != null)
			listener.onAuthPassingError(auth, e);
	}

	private void showError(Throwable e) {
		boolean serious = true;
		String langpath = "unknown";

		if (e instanceof AuthenticatorException) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException)
				serious = false;
			if (showErrorOnce && errorShown && !serious)
				return;

			AuthenticatorException ae = (AuthenticatorException) e;
			langpath = (ae.getLangpath() == null) ? "unknown" : ae
					.getLangpath();

			e = null; // Mark as known, don't show stack trace
		}

		Alert.showLocError("auth.error.title", "auth.error." + langpath, e);

		if (!serious)
			this.errorShown = true;
	}

	@Override
	public void onAuthPassed(Authenticator auth) {
		if (listener != null)
			listener.onAuthPassed(auth);

		saveProfiles();
	}

	public void saveProfiles() {
		try {
			TLauncher.getInstance().getProfileManager().saveProfiles();
		} catch (IOException e) {
			Alert.showLocError("auth.profiles.save-error");
		}
	}

}
