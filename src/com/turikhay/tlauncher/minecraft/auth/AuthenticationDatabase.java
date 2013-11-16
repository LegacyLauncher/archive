package com.turikhay.tlauncher.minecraft.auth;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class AuthenticationDatabase {
   private Map authById;

   public AuthenticationDatabase() {
      this(new HashMap());
   }

   public AuthenticationDatabase(Map authById) {
      this.authById = authById;
   }

   public Authenticator getByName(String name) {
      if (name == null) {
         return null;
      } else {
         Iterator var3 = this.authById.entrySet().iterator();

         Entry entry;
         GameProfile profile;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            entry = (Entry)var3.next();
            profile = ((Authenticator)entry.getValue()).getSelectedProfile();
         } while(profile == null || !profile.getName().equals(name));

         return (Authenticator)entry.getValue();
      }
   }

   public Authenticator getByUUID(String uuid) {
      return (Authenticator)this.authById.get(uuid);
   }

   public List getKnownNames() {
      List names = new ArrayList();
      Iterator var3 = this.authById.entrySet().iterator();

      while(var3.hasNext()) {
         Entry entry = (Entry)var3.next();
         GameProfile profile = ((Authenticator)entry.getValue()).getSelectedProfile();
         if (profile != null) {
            names.add(profile.getName());
         }
      }

      return names;
   }

   public void register(String uuid, Authenticator authentication) {
      this.authById.put(uuid, authentication);
   }

   public Set getknownUUIDs() {
      return this.authById.keySet();
   }

   public void removeUUID(String uuid) {
      this.authById.remove(uuid);
   }

   public Collection getAuthenticators() {
      return this.authById.values();
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public JsonElement serialize(AuthenticationDatabase src, Type typeOfSrc, JsonSerializationContext context) {
         Map services = src.authById;
         Map credentials = new HashMap();
         Iterator var7 = services.entrySet().iterator();

         while(var7.hasNext()) {
            Entry en = (Entry)var7.next();
            credentials.put((String)en.getKey(), ((Authenticator)en.getValue()).createMap());
         }

         return context.serialize(credentials);
      }

      public AuthenticationDatabase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         TypeToken token = new TypeToken() {
         };
         Map services = new HashMap();
         Map credentials = (Map)context.deserialize(json, token.getType());
         Iterator var8 = credentials.entrySet().iterator();

         while(var8.hasNext()) {
            Entry en = (Entry)var8.next();
            services.put((String)en.getKey(), Authenticator.createFromMap((Map)en.getValue()));
         }

         return new AuthenticationDatabase(services);
      }
   }
}
