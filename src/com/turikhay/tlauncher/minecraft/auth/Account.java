package com.turikhay.tlauncher.minecraft.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Account {
   protected String username;
   protected String userID;
   protected String displayName;
   protected String password;
   protected String accessToken;
   protected String uuid;
   protected List userProperties;
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

   public boolean hasUsername() {
      return this.username != null;
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
      if ("null".equals(accessToken)) {
         accessToken = null;
      }

      this.accessToken = accessToken;
      this.type = accessToken == null ? Account.AccountType.PIRATE : Account.AccountType.LICENSE;
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
      List list = new ArrayList();
      Map properties;
      Iterator var4;
      if (this.userProperties != null) {
         var4 = this.userProperties.iterator();

         while(var4.hasNext()) {
            properties = (Map)var4.next();
            list.add(new UserProperty((String)properties.get("name"), (String)properties.get("value")));
         }
      }

      if (this.user != null && this.user.getProperties() != null) {
         var4 = this.user.getProperties().iterator();

         while(var4.hasNext()) {
            properties = (Map)var4.next();
            list.add(new UserProperty((String)properties.get("name"), (String)properties.get("value")));
         }
      }

      var4 = list.iterator();

      while(var4.hasNext()) {
         UserProperty property = (UserProperty)var4.next();
         List values = new ArrayList();
         values.add(property.getValue());
         map.put(property.getKey(), values);
      }

      return map;
   }

   public void setProperties(List properties) {
      this.userProperties = properties;
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

      if (this.userProperties != null) {
         r.put("userProperties", this.userProperties);
      }

      return r;
   }

   public void fillFromMap(Map map) {
      if (map.containsKey("username")) {
         this.setUsername(map.get("username").toString());
      }

      this.setUserID(map.containsKey("userid") ? map.get("userid").toString() : this.getUsername());
      this.setDisplayName(map.containsKey("displayName") ? map.get("displayName").toString() : this.getUsername());
      this.setProperties(map.containsKey("userProperties") ? (List)map.get("userProperties") : null);
      this.setUUID(map.containsKey("uuid") ? map.get("uuid").toString() : null);
      this.setAccessToken(map.containsKey("accessToken") ? map.get("accessToken").toString() : null);
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

   public String toDebugString() {
      return "Account" + this.createMap();
   }

   public static Account randomAccount() {
      return new Account("random" + (new Random()).nextLong());
   }

   public static enum AccountType {
      LICENSE,
      PIRATE;

      public String toString() {
         return super.toString().toLowerCase();
      }
   }
}
