package com.turikhay.tlauncher.minecraft.auth;

public class AuthResponse extends RefreshResponse {
	private User user;

	public User getUser() {
		return this.user;
	}
	
	public class User {
		private String id;
		public String getId() { return this.id; }
	}
}
