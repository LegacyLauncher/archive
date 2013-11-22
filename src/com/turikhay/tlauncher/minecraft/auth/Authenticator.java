package com.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.launcher.Http;
import org.apache.commons.lang3.StringUtils;

public class Authenticator {
   public static final URL ROUTE_AUTHENTICATE = Http.constantURL("https://authserver.mojang.com/authenticate");
   public static final URL ROUTE_REFRESH = Http.constantURL("https://authserver.mojang.com/refresh");
   public static final URL ROUTE_VALIDATE = Http.constantURL("https://authserver.mojang.com/validate");
   public static final URL ROUTE_INVALIDATE = Http.constantURL("https://authserver.mojang.com/invalidate");
   public static final URL ROUTE_SIGNOUT = Http.constantURL("https://authserver.mojang.com/signout");
   private String username;
   private String password;
   private String clientToken;
   private String accessToken;
   private String uuid;
   private String userid;
   private String displayName;
   private boolean online;
   private GameProfile[] profiles;
   private GameProfile profile;
   private final Authenticator instance = this;
   private final Gson gson = new Gson();

   public String toString() {
      return "Authenticator" + this.createMap();
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String name) {
      if (StringUtils.isBlank(name)) {
         throw new IllegalArgumentException("Username is blank!");
      } else {
         this.username = name;
      }
   }

   public void setPassword(String pass) {
      if (StringUtils.isBlank(pass)) {
         throw new IllegalArgumentException("Password is blank!");
      } else {
         this.password = pass;
      }
   }

   public void setClientToken(String tok) {
      if (StringUtils.isBlank(tok)) {
         throw new IllegalArgumentException("Client token is blank!");
      } else {
         this.clientToken = tok;
      }
   }

   public void setAccessToken(String tok) {
      if (StringUtils.isBlank(tok)) {
         throw new IllegalArgumentException("Access token is blank!");
      } else {
         this.accessToken = tok;
      }
   }

   public void setUUID(String id) {
      this.uuid = id;
   }

   public void setUserID(String id) {
      this.userid = id;
   }

   public void setDisplayName(String name) {
      this.displayName = name;
   }

   public void selectGameProfile(GameProfile gprofile) {
      if (gprofile == null) {
         throw new NullPointerException("GameProfile is NULL!");
      } else {
         this.profile = gprofile;
      }
   }

   public boolean selectGameProfile() {
      if (this.displayName != null && this.uuid != null) {
         this.selectGameProfile(new GameProfile(this.uuid, this.displayName));
         return true;
      } else {
         return false;
      }
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getPassword() {
      return this.password;
   }

   public String getClientToken() {
      return this.clientToken;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public String getUUID() {
      return this.uuid;
   }

   public String getUserID() {
      return this.userid;
   }

   public boolean isOnline() {
      return this.online;
   }

   public GameProfile[] getAvailableProfiles() {
      return this.profiles;
   }

   public GameProfile getSelectedProfile() {
      return this.profile;
   }

   public void pass() throws AuthenticatorException {
      if (this.password == null && this.accessToken == null) {
         throw new AuthenticatorException("Password and token are NULL!");
      } else if (this.password != null && this.accessToken != null) {
         throw new AuthenticatorException("Cannot choose authentication type between \"password\" and \"token\": both are not NULL.");
      } else {
         this.log("Staring to authenticate:");
         this.log("Username:", this.username);
         this.log("Password:", this.password);
         this.log("Client token:", this.clientToken);

         try {
            if (this.accessToken == null) {
               this.passwordLogin();
            } else {
               this.tokenLogin();
            }
         } catch (IOException var2) {
            throw new AuthenticatorException("Cannot log in!", var2);
         }

         this.log("Log in successful!");
         this.log("UUID:", this.uuid);
         this.log("Client token:", this.clientToken);
         this.log("Access token:", this.accessToken);
         this.log("Profiles:", this.profiles);
         this.log("Selected profile:", this.profile);
      }
   }

   protected void passwordLogin() throws AuthenticatorException, IOException {
      this.log("Loggining in with password");
      AuthenticationRequest request = new AuthenticationRequest(this);
      AuthenticationResponse response = (AuthenticationResponse)this.makeRequest(ROUTE_AUTHENTICATE, request, AuthenticationResponse.class);
      this.userid = response.getUserID() != null ? response.getUserID() : this.getUsername();
      this.clientToken = response.getClientToken();
      this.accessToken = response.getAccessToken();
      this.profiles = response.getAvailableProfiles();
      this.profile = response.getSelectedProfile();
      this.online = true;
      if (this.profile != null) {
         this.uuid = response.getSelectedProfile().getId();
         this.displayName = response.getSelectedProfile().getName();
      }

   }

   protected void tokenLogin() throws AuthenticatorException, IOException {
      this.log("Loggining in with password");
      RefreshRequest request = new RefreshRequest(this);
      RefreshResponse response = (RefreshResponse)this.makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
      this.accessToken = response.getAccessToken();
      this.profiles = response.getAvailableProfiles();
      this.profile = response.getSelectedProfile();
      this.online = true;
   }

   protected Response makeRequest(URL url, Request input, Class classOfT) throws AuthenticatorException, IOException {
      this.log("Making request:", url);
      this.log(this.gson.toJson((Object)input));
      String jsonResult = input == null ? Http.performGet(url) : Http.performPost(url, this.gson.toJson((Object)input), "application/json");
      this.log("Result:", jsonResult);
      Response result = (Response)this.gson.fromJson(jsonResult, classOfT);
      if (result == null) {
         return null;
      } else if (result.getClass() != classOfT) {
         throw new RuntimeException("Result class is invalid!");
      } else if (StringUtils.isBlank(result.getError())) {
         return result;
      } else if ("UserMigratedException".equals(result.getCause())) {
         throw new AuthenticatorException(result.getErrorMessage(), "migrated");
      } else if (result.getError().equals("ForbiddenOperationException")) {
         throw new AuthenticatorException(result.getErrorMessage(), "forbidden");
      } else {
         throw new AuthenticatorException(result.getErrorMessage(), "internal");
      }
   }

   public void asyncPass(final AuthenticatorListener l) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (l != null) {
               l.onAuthPassing(Authenticator.this.instance);
            }

            try {
               Authenticator.this.instance.pass();
            } catch (Exception var2) {
               if (l != null) {
                  l.onAuthPassingError(Authenticator.this.instance, var2);
               }

               return;
            }

            if (l != null) {
               l.onAuthPassed(Authenticator.this.instance);
            }

         }
      });
   }

   public Map createMap() {
      Map r = new HashMap();
      r.put("username", this.username);
      r.put("accessToken", this.accessToken);
      r.put("userid", this.userid);
      r.put("uuid", this.uuid);
      r.put("displayName", this.displayName);
      return r;
   }

   public static Authenticator createFromMap(Map map) {
      Authenticator auth = new Authenticator();
      auth.setUsername((String)map.get("username"));
      auth.setUserID(map.containsKey("userid") ? (String)map.get("userid") : auth.getUsername());
      auth.setDisplayName(map.containsKey("displayName") ? (String)map.get("displayName") : auth.getUsername());
      auth.setUUID((String)map.get("uuid"));
      auth.setAccessToken((String)map.get("accessToken"));
      auth.selectGameProfile();
      return auth;
   }

   protected void log(Object... o) {
      U.log("[AUTH]", o);
   }
}
