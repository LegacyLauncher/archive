package com.turikhay.tlauncher.minecraft.auth;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.launcher.Http;

import com.google.gson.Gson;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.U;

public class Authenticator {
	public static final URL ROUTE_AUTHENTICATE = Http.constantURL("https://authserver.mojang.com/authenticate");
	public static final URL ROUTE_REFRESH = Http.constantURL("https://authserver.mojang.com/refresh");
	public static final URL ROUTE_VALIDATE = Http.constantURL("https://authserver.mojang.com/validate");
	public static final URL ROUTE_INVALIDATE = Http.constantURL("https://authserver.mojang.com/invalidate");
	public static final URL ROUTE_SIGNOUT = Http.constantURL("https://authserver.mojang.com/signout");
	
	private String username;
	private String password, clientToken, accessToken, uuid, userid, displayName;
	private boolean online;
	private GameProfile[] profiles;
	private GameProfile profile;
	
	private final Authenticator instance = this;
	private final Gson gson = new Gson();
	
	public String toString(){
		return "Authenticator" + this.createMap();
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String name){		
		if(StringUtils.isBlank(name))
			throw new IllegalArgumentException("Username is blank!");
		
		username = name;
	}
	
	public void setPassword(String pass){		
		if(StringUtils.isBlank(pass))
			throw new IllegalArgumentException("Password is blank!");
		
		password = pass;
	}
	
	public void setPassword(char[] pass){		
		setPassword(new String(pass));
	}
	
	public void setClientToken(String tok){	
		if(StringUtils.isBlank(tok))
			throw new IllegalArgumentException("Client token is blank!");
		
		clientToken = tok;
	}
	
	public void setClientToken(UUID uid){	
		setClientToken(uid.toString());
	}
	
	public void setAccessToken(String tok){		
		if(StringUtils.isBlank(tok))
			throw new IllegalArgumentException("Access token is blank!");
		
		accessToken = tok;
	}
	
	public void setUUID(String id){		
		uuid = id;
	}
	
	public void setUserID(String id){		
		userid = id;
	}
	
	public void setDisplayName(String name){		
		displayName = name;
	}
	
	public void selectGameProfile(GameProfile gprofile){
		if(gprofile == null)
			throw new NullPointerException("GameProfile is NULL!");
		
		profile = gprofile;
	}
	
	public boolean selectGameProfile(){
		if(displayName == null || uuid == null) return false;
		
		this.selectGameProfile(new GameProfile(uuid, displayName));
		return true;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getClientToken(){
		return clientToken;
	}
	
	public String getAccessToken(){
		return accessToken;
	}
	
	public String getUUID(){
		return uuid;
	}
	
	public String getUserID(){
		return userid;
	}
	
	public boolean isOnline(){
		return online;
	}
	
	public GameProfile[] getAvailableProfiles() {
		return this.profiles;
	}

	public GameProfile getSelectedProfile() {
		return this.profile;
	}
	
	public void pass() throws AuthenticatorException {
		if(password == null && accessToken == null)
			throw new AuthenticatorException("Password and token are NULL!");
		if(password != null && accessToken != null)
			throw new AuthenticatorException("Cannot choose authentication type between \"password\" and \"token\": both are not NULL.");
		
		log("Staring to authenticate:");
		log("Username:", username);
		log("Password:", password);
		log("Client token:", clientToken);
		
		try{
			if(accessToken == null) passwordLogin();
			else tokenLogin();
		}catch(IOException ex){
			throw new AuthenticatorException("Cannot log in!", ex);
		}
		
		log("Log in successful!");
		
		log("UUID:", uuid);
		log("Client token:", clientToken);
		log("Access token:", accessToken);
		log("Profiles:", profiles);
		log("Selected profile:", profile);
	}
	
	protected void passwordLogin() throws AuthenticatorException, IOException {
		log("Loggining in with password");
		
	    AuthenticationRequest request = new AuthenticationRequest(this);
	    AuthenticationResponse response = makeRequest(ROUTE_AUTHENTICATE, request, AuthenticationResponse.class);
	    
	    this.userid = (response.getUserID() != null)? response.getUserID() : getUsername();
	    this.clientToken = response.getClientToken();
	    this.accessToken = response.getAccessToken();
	    this.profiles = response.getAvailableProfiles();
	    this.profile = response.getSelectedProfile();
	    this.online = true;
	    
	    if(profile != null){
	    	this.uuid = response.getSelectedProfile().getId();
	    	this.displayName = response.getSelectedProfile().getName();
	    }
	}
	
	protected void tokenLogin() throws AuthenticatorException, IOException {
		log("Loggining in with password");
		
	    RefreshRequest request = new RefreshRequest(this);
	    RefreshResponse response = makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
	    
	    this.accessToken = response.getAccessToken();
	    this.profiles = response.getAvailableProfiles();
	    this.profile = response.getSelectedProfile();
	    this.online = true;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Response> T makeRequest(URL url, Request input, Class<T> classOfT) throws AuthenticatorException, IOException {
		log("Making request:", url);
		log(this.gson.toJson(input));
		String jsonResult = input == null ? Http.performGet(url) : Http.performPost(url, this.gson.toJson(input), "application/json");
		log("Result:", jsonResult);
		Response result = this.gson.fromJson(jsonResult, classOfT);

		if(result == null) return null;
		
		if(result.getClass() != classOfT)
			throw new RuntimeException("Result class is invalid!");

		if(StringUtils.isBlank( result.getError() )) return (T) result;
		
		if("UserMigratedException".equals(result.getCause()))
			throw new AuthenticatorException(result.getErrorMessage(), "migrated");
	          
		if(result.getError().equals("ForbiddenOperationException"))
			throw new AuthenticatorException(result.getErrorMessage(), "forbidden");
	          
		throw new AuthenticatorException(result.getErrorMessage(), "internal");
	}
	
	public void asyncPass(final AuthenticatorListener l) {		
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(l != null) l.onAuthPassing(instance);
				
				try{ instance.pass(); }catch(Exception e){
					if(l != null) l.onAuthPassingError(instance, e);
					return;
				}
				
				if(l != null) l.onAuthPassed(instance);
			}
		});
	}
	
	
	public Map<String, String> createMap(){
		Map<String, String> r = new HashMap<String, String>();
		
		r.put("username", username);
		r.put("accessToken", accessToken);
		r.put("userid", userid);
		r.put("uuid", uuid);
		r.put("displayName", displayName);
		
		return r;
	}
	
	public static Authenticator createFromMap(Map<String, String> map){
		Authenticator auth = new Authenticator();
		
		auth.setUsername(map.get("username"));
		auth.setUserID(map.containsKey("userid")? map.get("userid") : auth.getUsername());
		auth.setDisplayName(map.containsKey("displayName")? map.get("displayName") : auth.getUsername());
		auth.setUUID(map.get("uuid"));
		auth.setAccessToken(map.get("accessToken"));
		
		auth.selectGameProfile();
		
		return auth;
	}
	
	protected void log(Object...o){ U.log("[AUTH]", o); }
}
