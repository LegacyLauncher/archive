package com.turikhay.tlauncher.minecraft.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class AuthenticationDatabase
{
	private Map<String, Authenticator> authById;

	public AuthenticationDatabase() {
		this(new HashMap<String, Authenticator>());
	}

	public AuthenticationDatabase(Map<String, Authenticator> authById) {
		this.authById = authById;
	}

	public Authenticator getByName(String name) {
		if (name == null) return null;

		for (Entry<String, Authenticator> entry : this.authById.entrySet()) {
			GameProfile profile = entry.getValue().getSelectedProfile();

			if((profile != null) && (profile.getName().equals(name)))
				return entry.getValue();
		}
		
		return null;
	}

	public Authenticator getByUUID(String uuid) {
		return this.authById.get(uuid);
	}

	public List<String> getKnownNames() {
		List<String> names = new ArrayList<String>();

		for (Entry<String, Authenticator> entry : this.authById.entrySet()) {
			GameProfile profile = entry.getValue().getSelectedProfile();

			if (profile != null)
				names.add(profile.getName());
		}

		return names;
	}

	public void register(String uuid, Authenticator authentication) {
		this.authById.put(uuid, authentication);
	}

	public Set<String> getknownUUIDs() {
		return this.authById.keySet();
	}

	public void removeUUID(String uuid) {
		this.authById.remove(uuid);
	}
	
	public Collection<Authenticator> getAuthenticators(){
		return this.authById.values();
	}

	// OH SHI~
	public static class Serializer
		implements JsonDeserializer<AuthenticationDatabase>, JsonSerializer<AuthenticationDatabase> {
    public JsonElement serialize(AuthenticationDatabase src, Type typeOfSrc, JsonSerializationContext context)
    {
    	Map<String, Authenticator> services = src.authById;
    	Map<String, Map<String, String>> credentials = new HashMap<String, Map<String, String>>();
    	
    	for(Entry<String, Authenticator> en : services.entrySet())
    		credentials.put(en.getKey(), en.getValue().createMap());
    	
    	return context.serialize(credentials);
    }

	public AuthenticationDatabase deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		// Go deeper, muzafaker
		// O:
		TypeToken<HashMap<String, Map<String, String>>> token = new TypeToken<HashMap<String, Map<String, String>>>(){};
	      
		Map<String, Authenticator> services = new HashMap<String, Authenticator>();
		Map<String, Map<String, String>> credentials = context.deserialize(json, token.getType());
	      
		for(Entry<String, Map<String, String>> en : credentials.entrySet())
			services.put(en.getKey(), Authenticator.createFromMap(en.getValue()));
	      
		return new AuthenticationDatabase(services);
	}
  }
}
