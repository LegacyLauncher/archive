package com.turikhay.tlauncher.minecraft.auth;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AuthenticatorDatabase {

	private final Map<String, Account> accounts;

	private AccountListener listener;

	public AuthenticatorDatabase(Map<String, Account> map) {
		if (map == null)
			throw new NullPointerException();

		this.accounts = map;
	}

	public AuthenticatorDatabase() {
		this(new LinkedHashMap<String, Account>());
	}

	public Collection<Account> getAccounts() {
		return Collections.unmodifiableCollection(accounts.values());
	}

	public Account getByUsername(String username) {
		if (username == null)
			throw new NullPointerException();

		for (Account acc : accounts.values())
			if (username.equals(acc.getUsername()))
				return acc;

		return null;
	}

	public void registerAccount(Account acc) {
		if (acc == null)
			throw new NullPointerException();

		if (accounts.containsValue(acc))
			throw new IllegalArgumentException("This account already exists!");

		String uuid = (acc.getUUID() == null) ? acc.getUsername() : acc
				.getUUID();

		accounts.put(uuid, acc);
		fireRefresh();
	}

	public void unregisterAccount(Account acc) {
		if (acc == null)
			throw new NullPointerException();

		if (!accounts.containsValue(acc))
			throw new IllegalArgumentException("This account doesn't exist!");

		accounts.values().remove(acc);
		fireRefresh();
	}

	private void fireRefresh() {
		if (listener == null)
			return;
		listener.onAccountsRefreshed(this);
	}

	public void setListener(AccountListener listener) {
		this.listener = listener;
	}

	/*
	 * public static class Serializer implements
	 * JsonDeserializer<AuthenticatorDatabase>,
	 * JsonSerializer<AuthenticatorDatabase> {
	 * 
	 * @Override public JsonElement serialize(AuthenticatorDatabase src, Type
	 * typeOfSrc, JsonSerializationContext context) {
	 * 
	 * Map<String, Account> services = src.accounts; Map<String, Map<String,
	 * String>> credentials = new LinkedHashMap<String, Map<String, String>>();
	 * 
	 * for(Entry<String, Account> en : services.entrySet())
	 * credentials.put(en.getKey(), en.getValue().createMap());
	 * 
	 * return context.serialize(credentials); }
	 * 
	 * @Override public AuthenticatorDatabase deserialize(JsonElement json, Type
	 * typeOfT, JsonDeserializationContext context) throws JsonParseException {
	 * TypeToken<LinkedHashMap<String, Map<String, String>>> token = new
	 * TypeToken<LinkedHashMap<String, Map<String, String>>>(){};
	 * 
	 * Map<String, Account> services = new LinkedHashMap<String, Account>();
	 * Map<String, Map<String, String>> credentials = context.deserialize(json,
	 * token.getType());
	 * 
	 * for(Entry<String, Map<String, String>> en : credentials.entrySet())
	 * services.put(en.getKey(), new Account(en.getValue()));
	 * 
	 * return new AuthenticatorDatabase(services); }
	 * 
	 * }
	 */

	// OH SHI~
	public static class Serializer implements
			JsonDeserializer<AuthenticatorDatabase>,
			JsonSerializer<AuthenticatorDatabase> {
		@Override
		public AuthenticatorDatabase deserialize(JsonElement json,
				Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			Map<String, Account> services = new LinkedHashMap<String, Account>();
			Map<String, Map<String, Object>> credentials = deserializeCredentials(
					(JsonObject) json, context);

			for (Entry<String, Map<String, Object>> en : credentials.entrySet())
				services.put(en.getKey(), new Account(en.getValue()));

			return new AuthenticatorDatabase(services);
		}

		Map<String, Map<String, Object>> deserializeCredentials(
				JsonObject json, JsonDeserializationContext context) {
			Map<String, Map<String, Object>> result = new LinkedHashMap<String, Map<String, Object>>();

			for (Map.Entry<String, JsonElement> authEntry : json.entrySet()) {
				Map<String, Object> credentials = new LinkedHashMap<String, Object>();
				for (Map.Entry<String, JsonElement> credentialsEntry : ((JsonObject) authEntry
						.getValue()).entrySet())
					credentials.put(credentialsEntry.getKey(),
							deserializeCredential(credentialsEntry.getValue()));
				result.put(authEntry.getKey(), credentials);
			}

			return result;
		}

		private Object deserializeCredential(JsonElement element) {
			if (element instanceof JsonObject) {
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				for (Map.Entry<String, JsonElement> entry : ((JsonObject) element)
						.entrySet())
					result.put(entry.getKey(),
							deserializeCredential(entry.getValue()));
				return result;
			}

			if (element instanceof JsonArray) {
				List<Object> result = new ArrayList<Object>();
				for (JsonElement entry : (JsonArray) element)
					result.add(deserializeCredential(entry));
				return result;
			}

			return element.getAsString();
		}

		@Override
		public JsonElement serialize(AuthenticatorDatabase src, Type typeOfSrc,
				JsonSerializationContext context) {

			Map<String, Account> services = src.accounts;
			Map<String, Map<String, Object>> credentials = new LinkedHashMap<String, Map<String, Object>>();

			for (Entry<String, Account> en : services.entrySet())
				credentials.put(en.getKey(), en.getValue().createMap());

			return context.serialize(credentials);
		}
	}
}
