package com.turikhay.tlauncher.minecraft.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Account {
   protected String username;
   protected String userID;
   protected String displayName;
   protected String password;
   protected String accessToken;
   protected String uuid;
   protected Account.AccountType type;
   protected GameProfile[] profiles;
   protected GameProfile selectedProfile;
   protected User user;
   private final Authenticator auth;

   public Account() {
      this.type = Account.AccountType.PIRATE;
      this.auth = new Authenticator(this);
   }

   public Account(String username) {
      this();
      this.setUsername(username);
   }

   public Account(Map map) {
      this();
      this.fillFromMap(map);
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getUserID() {
      return this.userID;
   }

   public void setUserID(String userID) {
      this.userID = userID;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
      if (password != null) {
         this.type = Account.AccountType.LICENSE;
      }

   }

   public void setPassword(char[] password) {
      this.setPassword(new String(password));
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
      if (accessToken != null) {
         this.type = Account.AccountType.LICENSE;
      }

   }

   public String getUUID() {
      return this.uuid;
   }

   public void setUUID(String uuid) {
      this.uuid = uuid;
   }

   public GameProfile[] getProfiles() {
      return this.profiles;
   }

   public void setProfiles(GameProfile[] p) {
      this.profiles = p;
   }

   public GameProfile getProfile() {
      return this.selectedProfile != null ? this.selectedProfile : GameProfile.DEFAULT_PROFILE;
   }

   public void setProfile(GameProfile p) {
      this.selectedProfile = p;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public User getUser() {
      return this.user;
   }

   public void setUser(User user) {
      if (user == null) {
         throw new NullPointerException();
      } else {
         this.user = user;
      }
   }

   public Map getProperties() {
      Map map = new HashMap();
      if (this.user == null) {
         return map;
      } else {
         List properties = this.user.getProperties();
         if (properties != null && !properties.isEmpty()) {
            Iterator var4 = properties.iterator();

            while(var4.hasNext()) {
               User.Property property = (User.Property)var4.next();
               List values = new ArrayList();
               values.add(property.getValue());
               map.put(property.getKey(), values);
            }

            return map;
         } else {
            return map;
         }
      }
   }

   public Account.AccountType getType() {
      return this.type;
   }

   public void setType(Account.AccountType type) {
      if (type == null) {
         throw new NullPointerException();
      } else {
         this.type = type;
      }
   }

   public boolean hasLicense() {
      return this.type.equals(Account.AccountType.LICENSE);
   }

   public void setHasLicense(boolean has) {
      this.setType(has ? Account.AccountType.LICENSE : Account.AccountType.PIRATE);
   }

   public Authenticator getAuthenticator() {
      return this.auth;
   }

   public Map createMap() {
      Map r = new HashMap();
      r.put("username", this.username);
      r.put("userid", this.userID);
      r.put("uuid", this.uuid);
      r.put("displayName", this.displayName);
      if (this.hasLicense()) {
         r.put("accessToken", this.accessToken);
      }

      return r;
   }

   public void fillFromMap(Map map) {
      this.setUsername((String)map.get("username"));
      this.setUserID(map.containsKey("userid") ? (String)map.get("userid") : this.getUsername());
      this.setDisplayName(map.containsKey("displayName") ? (String)map.get("displayName") : this.getUsername());
      this.setUUID((String)map.get("uuid"));
      this.setAccessToken((String)map.get("accessToken"));
      this.setType(map.containsKey("accessToken") ? Account.AccountType.LICENSE : Account.AccountType.PIRATE);
   }

   public void complete(Account acc) {
      if (acc == null) {
         throw new NullPointerException();
      } else {
         boolean sameName = acc.username.equals(this.username);
         this.username = acc.username;
         this.type = acc.type;
         if (acc.userID != null) {
            this.userID = acc.userID;
         }

         if (acc.displayName != null) {
            this.displayName = acc.displayName;
         }

         if (acc.password != null) {
            this.password = acc.password;
         }

         if (!sameName) {
            this.accessToken = null;
         }

      }
   }

   public boolean equals(Account acc) {
      if (acc == null) {
         return false;
      } else if (this.username == null) {
         return acc.username == null;
      } else {
         boolean pass = this.password != null ? this.password.equals(acc.password) : true;
         return this.username.equals(acc.username) && this.type.equals(acc.type) && pass;
      }
   }

   public String toString() {
      return this.username == null ? "..." : this.username + (this.displayName != null && this.hasLicense() ? " (" + this.displayName + ")" : "");
   }

   public static enum AccountType {
      LICENSE,
      PIRATE;

      public String toString() {
         return super.toString().toLowerCase();
      }
   }
}
