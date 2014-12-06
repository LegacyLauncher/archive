package ru.turikhay.tlauncher.minecraft.auth;

class RefreshResponse extends Response {
	private String accessToken;
	private String clientToken;
	private GameProfile selectedProfile;
	private User user;

	public String getAccessToken() {
		return this.accessToken;
	}

	public String getClientToken() {
		return this.clientToken;
	}

	public GameProfile getSelectedProfile() {
		return this.selectedProfile;
	}

	public User getUser() {
		return user;
	}
}
