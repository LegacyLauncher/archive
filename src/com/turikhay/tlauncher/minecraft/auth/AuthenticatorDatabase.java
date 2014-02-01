package com.turikhay.tlauncher.minecraft.auth;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AuthenticatorDatabase {
   private final Map accounts;
   private AccountListener listener;

   public AuthenticatorDatabase(Map map) {
      if (map == null) {
         throw new NullPointerException();
      } else {
         this.accounts = map;
      }
   }

   public AuthenticatorDatabase() {
      this(new LinkedHashMap());
   }

   public Collection getAccounts() {
      return Collections.unmodifiableCollection(this.accounts.values());
   }

   public Account getByUsername(String username) {
      if (username == null) {
         throw new NullPointerException();
      } else {
         Iterator var3 = this.accounts.values().iterator();

         while(var3.hasNext()) {
            Account acc = (Account)var3.next();
            if (acc.getUsername().equals(username)) {
               return acc;
            }
         }

         return null;
      }
   }

   public void registerAccount(Account acc) {
      if (acc == null) {
         throw new NullPointerException();
      } else if (this.accounts.containsValue(acc)) {
         throw new IllegalArgumentException("This account already exists!");
      } else {
         String uuid = acc.getUUID() == null ? acc.getUsername() : acc.getUUID();
         this.accounts.put(uuid, acc);
         this.fireRefresh();
      }
   }

   public void unregisterAccount(Account acc) {
      if (acc == null) {
         throw new NullPointerException();
      } else if (!this.accounts.containsValue(acc)) {
         throw new IllegalArgumentException("This account doesn't exist!");
      } else {
         this.accounts.values().remove(acc);
         this.fireRefresh();
      }
   }

   private void fireRefresh() {
      if (this.listener != null) {
         this.listener.onAccountsRefreshed(this);
      }
   }

   public void setListener(AccountListener listener) {
      this.listener = listener;
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public JsonElement serialize(AuthenticatorDatabase src, Type typeOfSrc, JsonSerializationContext context) {
         Map services = src.accounts;
         Map credentials = new LinkedHashMap();
         Iterator var7 = services.entrySet().iterator();

         while(var7.hasNext()) {
            Entry en = (Entry)var7.next();
            credentials.put((String)en.getKey(), ((Account)en.getValue()).createMap());
         }

         return context.serialize(credentials);
      }

      public AuthenticatorDatabase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         TypeToken token = new TypeToken() {
         };
         Map services = new LinkedHashMap();
         Map credentials = (Map)context.deserialize(json, token.getType());
         Iterator var8 = credentials.entrySet().iterator();

         while(var8.hasNext()) {
            Entry en = (Entry)var8.next();
            services.put((String)en.getKey(), new Account((Map)en.getValue()));
         }

         return new AuthenticatorDatabase(services);
      }
   }
}
