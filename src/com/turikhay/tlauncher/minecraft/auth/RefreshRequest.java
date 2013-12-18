package com.turikhay.tlauncher.minecraft.auth;

public class RefreshRequest extends Request {
	private String clientToken, accessToken;
	private GameProfile selectedProfile;
	
	RefreshRequest(String clientToken, String accessToken, GameProfile profile){
		this.clientToken = clientToken;
		this.accessToken = accessToken;
		this.selectedProfile = profile;
	}
	
	RefreshRequest(String clientToken, String accessToken){
		this(clientToken, accessToken, null);
	}
	
	RefreshRequest(Authenticator auth, GameProfile profile){
		this(auth.getClientToken(), auth.getAccessToken(), profile);
	}
	
	RefreshRequest(Authenticator auth){
		this(auth, null);
	}
	
	public String getClientToken(){
		return clientToken;
	}
	
	public String getAccessToken(){
		return accessToken;
	}
	
	public GameProfile getProfile(){
		return selectedProfile;
	}
}
