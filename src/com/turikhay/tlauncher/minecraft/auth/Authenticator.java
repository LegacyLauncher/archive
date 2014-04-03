package com.turikhay.tlauncher.minecraft.auth;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import net.minecraft.launcher.Http;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;

public class Authenticator {
	private static final URL ROUTE_AUTHENTICATE = Http
			.constantURL("https://authserver.mojang.com/authenticate");
	private static final URL ROUTE_REFRESH = Http
			.constantURL("https://authserver.mojang.com/refresh");
	// private static final URL ROUTE_VALIDATE =
	// Http.constantURL("https://authserver.mojang.com/validate");
	// private static final URL ROUTE_INVALIDATE =
	// Http.constantURL("https://authserver.mojang.com/invalidate");
	// private static final URL ROUTE_SIGNOUT =
	// Http.constantURL("https://authserver.mojang.com/signout");

	public final Account account;

	private final Authenticator instance;
	private final Gson gson;

	public Authenticator(Account account) {
		if (account == null)
			throw new NullPointerException();

		this.instance = this;
		this.gson = new Gson();

		this.account = account;
	}

	public UUID getClientToken() {
		return TLauncher.getInstance().getProfileManager().getClientToken();
	}

	private void setClientToken(String uuid) {
		TLauncher.getInstance().getProfileManager().setClientToken(uuid);
	}

	void pass() throws AuthenticatorException {
		if (!account.hasLicense())
			throw new IllegalArgumentException("Invalid account type!");

		if (account.getPassword() == null && account.getAccessToken() == null)
			throw new AuthenticatorException("Password and token are NULL!");

		log("Staring to authenticate:");
		log("hasUsername:", account.getUsername());
		log("hasPassword:", account.getPassword() != null);
		log("hasAccessToken:", account.getAccessToken() != null);

		if (account.getPassword() == null)
			tokenLogin();
		else
			passwordLogin();

		log("Log in successful!");

		log("hasUUID:", account.getUUID() != null);
		log("hasAccessToken:", account.getAccessToken() != null);
		log("hasProfiles:", account.getProfiles() != null);
		log("hasProfile:", account.getProfiles() != null);
		log("hasProperties:", account.getProperties() != null);
	}

	public boolean pass(AuthenticatorListener l) {
		if (l != null)
			l.onAuthPassing(instance);

		try {
			instance.pass();
		} catch (Exception e) {
			log("Cannot authenticate:", e);
			if (l != null)
				l.onAuthPassingError(instance, e);
			return false;
		}
		if (l != null)
			l.onAuthPassed(instance);
		return true;
	}

	public void asyncPass(final AuthenticatorListener l) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				pass(l);
			}
		});
	}

	void passwordLogin() throws AuthenticatorException {
		log("Loggining in with password");

		AuthenticationRequest request = new AuthenticationRequest(this);
		AuthenticationResponse response = makeRequest(ROUTE_AUTHENTICATE,
				request, AuthenticationResponse.class);

		account.setUserID((response.getUserID() != null) ? response.getUserID()
				: account.getUsername());
		account.setAccessToken(response.getAccessToken());
		account.setProfiles(response.getAvailableProfiles());
		account.setProfile(response.getSelectedProfile());
		account.setUser(response.getUser());

		this.setClientToken(response.getClientToken());

		if (response.getSelectedProfile() != null) {
			account.setUUID(response.getSelectedProfile().getId());
			account.setDisplayName(response.getSelectedProfile().getName());
		}
	}

	void tokenLogin() throws AuthenticatorException {
		log("Loggining in with token");

		RefreshRequest request = new RefreshRequest(this);
		RefreshResponse response = makeRequest(ROUTE_REFRESH, request,
				RefreshResponse.class);

		this.setClientToken(response.getClientToken());

		account.setAccessToken(response.getAccessToken());
		account.setProfile(response.getSelectedProfile());
		account.setUser(response.getUser());
	}

	<T extends Response> T makeRequest(URL url, Request input, Class<T> classOfT)
			throws AuthenticatorException {
		String jsonResult;

		try {
			jsonResult = (input == null) ? AuthenticatorService
					.performGetRequest(url) : AuthenticatorService
					.performPostRequest(url, this.gson.toJson(input),
							"application/json");
		} catch (IOException e) {
			throw new AuthenticatorException(
					"Error making request, uncaught IOException",
					"unreachable", e);
		}

		T result = this.gson.fromJson(jsonResult, classOfT);

		if (result == null)
			return null;
		if (StringUtils.isBlank(result.getError()))
			return result;

		if ("UserMigratedException".equals(result.getCause()))
			throw new UserMigratedException();

		if (result.getError().equals("ForbiddenOperationException"))
			throw new InvalidCredentialsException();

		throw new AuthenticatorException(result.getErrorMessage(), "internal");
	}

	void log(Object... o) {
		U.log("[AUTH]", o);
	}
}
