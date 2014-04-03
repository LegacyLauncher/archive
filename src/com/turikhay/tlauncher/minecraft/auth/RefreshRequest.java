package com.turikhay.tlauncher.minecraft.auth;

class RefreshRequest extends Request {
	private String clientToken;
	private String accessToken;
	private GameProfile selectedProfile;

	@SuppressWarnings("unused")
	private boolean requestUser = true;

	private RefreshRequest(String clientToken, String accessToken,
			GameProfile profile) {
		this.clientToken = clientToken;
		this.accessToken = accessToken;
		this.selectedProfile = profile;
	}

	RefreshRequest(String clientToken, String accessToken) {
		this(clientToken, accessToken, null);
	}

	private RefreshRequest(Authenticator auth, GameProfile profile) {
		this(auth.getClientToken().toString(), auth.account.getAccessToken(),
				profile);
	}

	RefreshRequest(Authenticator auth) {
		this(auth, null);
	}

	public String getClientToken() {
		return clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public GameProfile getProfile() {
		return selectedProfile;
	}
}
