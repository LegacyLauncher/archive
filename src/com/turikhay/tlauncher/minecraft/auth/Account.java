package com.turikhay.tlauncher.minecraft.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Account {
	protected String username, userID, displayName;
	protected String password, accessToken, uuid;
	protected List<Map<String, String>> userProperties;
	
	protected AccountType type;
	
	protected GameProfile[] profiles;
	protected GameProfile selectedProfile;
	
	protected User user;
	
	private final Authenticator auth;
	
	public Account(){
		this.type = AccountType.PIRATE;
		this.auth = new Authenticator(this);
	}
	
	public Account(String username){
		this();
		this.setUsername(username);
	}
	
	public Account(Map<String, Object> map){
		this();
		this.fillFromMap(map);
	}
	
	public String getUsername(){
		return username;
	}
	
	public boolean hasUsername(){
		return username != null;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getUserID(){
		return userID;
	}
	
	public void setUserID(String userID){		
		this.userID = userID;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password){
		this.password = password;
		if(password != null) type = AccountType.LICENSE;
	}
	
	public void setPassword(char[] password){
		setPassword(new String(password));
	}
	
	public String getAccessToken(){
		return accessToken;
	}
	
	public void setAccessToken(String accessToken){
		this.accessToken = accessToken;
		if(accessToken != null) type = AccountType.LICENSE;
	}
	
	public String getUUID(){
		return uuid;
	}
	
	public void setUUID(String uuid){
		this.uuid = uuid;
	}
	
	public GameProfile[] getProfiles(){
		return profiles;
	}
	
	public void setProfiles(GameProfile[] p){
		this.profiles = p;
	}
	
	public GameProfile getProfile(){
		return (selectedProfile != null)? selectedProfile : GameProfile.DEFAULT_PROFILE;
	}
	
	public void setProfile(GameProfile p){
		this.selectedProfile = p;
	}
	
	public void setDisplayName(String displayName){		
		this.displayName = displayName;
	}
	
	public User getUser(){
		return user;
	}
	
	public void setUser(User user){
		if(user == null)
			throw new NullPointerException();
		
		this.user = user;
	}
	
	public Map<String, List<String>> getProperties(){
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<UserProperty> list = new ArrayList<UserProperty>();
		
		if(userProperties != null)
			for(Map<String, String> properties : userProperties)
				list.add(new UserProperty(properties.get("name"), properties.get("value")));
		
		if(user != null && user.getProperties() != null)
			for(Map<String, String> properties : user.getProperties())
				list.add(new UserProperty(properties.get("name"), properties.get("value")));
		
		for (UserProperty property : list) {
			List<String> values = new ArrayList<String>();
	        values.add(property.getValue());
	        
	        map.put(property.getKey(), values);
		}
		
		return map;
	}
	
	public void setProperties(List<Map<String, String>> properties){
		this.userProperties = properties;
	}
	
	public AccountType getType(){
		return type;
	}
	
	public void setType(AccountType type){
		if(type == null)
			throw new NullPointerException();
		
		this.type = type;		
	}
	
	public boolean hasLicense(){
		return type.equals(AccountType.LICENSE);
	}
	
	public void setHasLicense(boolean has){
		setType(has? AccountType.LICENSE : AccountType.PIRATE);
	}
	
	public Authenticator getAuthenticator(){
		return auth;
	}
	
	public Map<String, Object> createMap(){
		Map<String, Object> r = new HashMap<String, Object>();
		
		r.put("username", username);
		r.put("userid", userID);
		r.put("uuid", uuid);
		r.put("displayName", displayName);
		
		if(hasLicense())
			r.put("accessToken", accessToken);
		
		if(userProperties != null)
			r.put("userProperties", userProperties);
		
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public void fillFromMap(Map<String, Object> map){
		
		if(map.containsKey("username"))
			setUsername(map.get("username").toString());
		
		setUserID(map.containsKey("userid")? map.get("userid").toString() : getUsername());
		setDisplayName(map.containsKey("displayName")? map.get("displayName").toString() : getUsername());
		
		setProperties(map.containsKey("userProperties")? (List<Map<String, String>>) map.get("userProperties") : null);
		
		setUUID(map.containsKey("uuid")? map.get("uuid").toString() : null);
		setAccessToken(map.containsKey("accessToken")? map.get("accessToken").toString() : null);
		
		setType(map.containsKey("accessToken")? AccountType.LICENSE : AccountType.PIRATE);		
	}
	
	public void complete(Account acc){
		if(acc == null)
			throw new NullPointerException();
		
		boolean sameName = acc.username.equals(username);
		
		username = acc.username;
		type = acc.type;
		
		if(acc.userID != null) userID = acc.userID;
		if(acc.displayName != null) displayName = acc.displayName;
		if(acc.password != null) password = acc.password;
		if(!sameName) accessToken = null;
		
	}
	
	public boolean equals(Account acc){
		if(acc == null) return false;
		if(username == null) return acc.username == null;
		
		boolean
			pass = (password != null)? password.equals(acc.password) : true;
		
		return username.equals(acc.username) && type.equals(acc.type) && pass;
	}
	
	public String toString(){
		if(username == null) return "...";
		return username + (displayName != null && hasLicense()? " ("+displayName+")" : "");
	}
	
	public static Account randomAccount(){
		return new Account("random" + new Random().nextLong());
	}
	
	public enum AccountType {
		LICENSE, PIRATE;
		
		public String toString(){
			return super.toString().toLowerCase();
		}
	}
}
