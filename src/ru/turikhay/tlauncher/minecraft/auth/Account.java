package ru.turikhay.tlauncher.minecraft.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ru.turikhay.util.Reflect;

public class Account {
   private String username;
   private String userID;
   private String displayName;
   private String password;
   private String accessToken;
   private String uuid;
   private List userProperties;
   private Account.AccountType type;
   private GameProfile[] profiles;
   private GameProfile selectedProfile;
   private User user;

   public Account() {
      this.type = Account.AccountType.FREE;
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

   public void setUserID(String userID) {
      this.userID = userID;
   }

   public String getDisplayName() {
      return this.displayName == null ? this.username : this.displayName;
   }

   public String getPassword() {
      return this.password;
   }

   void setPassword(String password) {
      this.password = password;
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

   public void setUser(User user) {
      this.user = user;
   }

   public Map getProperties() {
      HashMap map = new HashMap();
      ArrayList list = new ArrayList();
      Map property;
      Iterator var4;
      if (this.userProperties != null) {
         var4 = this.userProperties.iterator();

         while(var4.hasNext()) {
            property = (Map)var4.next();
            list.add(new UserProperty((String)property.get("name"), (String)property.get("value")));
         }
      }

      if (this.user != null && this.user.getProperties() != null) {
         var4 = this.user.getProperties().iterator();

         while(var4.hasNext()) {
            property = (Map)var4.next();
            list.add(new UserProperty((String)property.get("name"), (String)property.get("value")));
         }
      }

      var4 = list.iterator();

      while(var4.hasNext()) {
         UserProperty property1 = (UserProperty)var4.next();
         ArrayList values = new ArrayList();
         values.add(property1.getValue());
         map.put(property1.getKey(), values);
      }

      return map;
   }

   void setProperties(List properties) {
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

   public boolean isFree() {
      return this.type.equals(Account.AccountType.FREE);
   }

   Map createMap() {
      HashMap r = new HashMap();
      r.put("username", this.username);
      r.put("userid", this.userID);
      r.put("uuid", UUIDTypeAdapter.toUUID(this.uuid));
      r.put("displayName", this.displayName);
      if (!this.isFree()) {
         r.put("type", this.type);
         r.put("accessToken", this.accessToken);
      }

      if (this.userProperties != null) {
         r.put("userProperties", this.userProperties);
      }

      return r;
   }

   void fillFromMap(Map map) {
      if (map.containsKey("username")) {
         this.setUsername(map.get("username").toString());
      }

      this.setUserID(map.containsKey("userid") ? map.get("userid").toString() : this.getUsername());
      this.setDisplayName(map.containsKey("displayName") ? map.get("displayName").toString() : this.getUsername());
      this.setProperties(map.containsKey("userProperties") ? (List)map.get("userProperties") : null);
      this.setUUID(map.containsKey("uuid") ? UUIDTypeAdapter.toUUID(map.get("uuid").toString()) : null);
      boolean hasAccessToken = map.containsKey("accessToken");
      if (hasAccessToken) {
         this.setAccessToken(map.get("accessToken").toString());
      }

      this.setType(map.containsKey("type") ? (Account.AccountType)Reflect.parseEnum(Account.AccountType.class, map.get("type").toString()) : (hasAccessToken ? Account.AccountType.MOJANG : Account.AccountType.FREE));
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
      return acc == null ? false : (this.username == null ? acc.username == null : this.username.equals(acc.username) && this.type.equals(acc.type) && (this.password == null || this.password.equals(acc.password)));
   }

   public String toString() {
      return this.toDebugString();
   }

   public String toDebugString() {
      Map map = this.createMap();
      if (map.containsKey("userProperties")) {
         map.remove("userProperties");
         map.put("userProperties", "(not null)");
      }

      if (map.containsKey("accessToken")) {
         map.remove("accessToken");
         map.put("accessToken", "(not null)");
      }

      return "Account" + map;
   }

   public static Account randomAccount() {
      return new Account("random" + (new Random()).nextLong());
   }

   public static enum AccountType {
      ELY,
      MOJANG,
      FREE;

      public String toString() {
         return super.toString().toLowerCase();
      }
   }
}
