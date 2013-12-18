package com.turikhay.tlauncher.minecraft.auth;

public class AuthenticationRequest extends Request {
	private Agent agent = Agent.MINECRAFT;
	private String username, password, clientToken;
	private boolean requestUser = true;

	AuthenticationRequest(String username, String password, String clientToken) {
		this.username = username;
		this.password = password;
	    this.clientToken = clientToken;
	}
	
	AuthenticationRequest(Authenticator auth){
		this(auth.getUsername(), auth.getPassword(), auth.getClientToken());
	}
	
	public Agent getAgent(){
		return agent;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getClientToken(){
		return clientToken;
	}
	
	public boolean isRequestingUser(){
		return requestUser;
	}
}
