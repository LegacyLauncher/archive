package com.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;
import java.io.IOException;
import java.net.URL;
import net.minecraft.launcher_.Http;
import org.apache.commons.lang3.StringUtils;

public class Authenticator {
   public static final URL ROUTE_AUTHENTICATE = Http.constantURL("https://authserver.mojang.com/authenticate");
   public static final URL ROUTE_REFRESH = Http.constantURL("https://authserver.mojang.com/refresh");
   public static final URL ROUTE_VALIDATE = Http.constantURL("https://authserver.mojang.com/validate");
   public static final URL ROUTE_INVALIDATE = Http.constantURL("https://authserver.mojang.com/invalidate");
   public static final URL ROUTE_SIGNOUT = Http.constantURL("https://authserver.mojang.com/signout");
   public final String username;
   private String password;
   private String clientToken;
   private String accessToken;
   private String uid;
   private boolean online;
   private GameProfile[] profiles;
   private GameProfile profile;
   private final Authenticator instance = this;
   private final Gson gson = new Gson();

   public Authenticator(String username) {
      if (username == null) {
         throw new IllegalArgumentException("Username cannot be NULL!");
      } else {
         this.username = username;
      }
   }

   public String getUsername() {
      return this.username;
   }

   public boolean setPassword(String pass) {
      if (this.password != null) {
         return false;
      } else if (StringUtils.isBlank(pass)) {
         throw new IllegalArgumentException("Password is blank!");
      } else {
         this.password = pass;
         return true;
      }
   }

   public boolean setClientToken(String tok) {
      if (this.clientToken != null) {
         return false;
      } else if (StringUtils.isBlank(tok)) {
         throw new IllegalArgumentException("Client token is blank!");
      } else {
         this.clientToken = tok;
         return true;
      }
   }

   public boolean setAccessToken(String tok) {
      if (this.accessToken != null) {
         return false;
      } else if (StringUtils.isBlank(tok)) {
         throw new IllegalArgumentException("Access token is blank!");
      } else {
         this.accessToken = tok;
         return true;
      }
   }

   public boolean setUID(String id) {
      if (this.uid != null) {
         return false;
      } else if (StringUtils.isBlank(id)) {
         throw new IllegalArgumentException("UID is blank!");
      } else {
         this.uid = id;
         return true;
      }
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

   public String getUID() {
      return this.uid;
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
      if (this.password == null && this.clientToken == null) {
         throw new AuthenticatorException("Password and token are NULL!");
      } else if (this.password != null && this.clientToken != null) {
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
         this.log("UID:", this.uid);
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
      this.uid = response.getUID() != null ? response.getUID() : this.getUsername();
      this.clientToken = response.getClientToken();
      this.accessToken = response.getAccessToken();
      this.profiles = response.getAvailableProfiles();
      this.profile = response.getSelectedProfile();
      this.online = true;
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

   protected void log(Object... o) {
      U.log("[AUTH]", o);
   }
}
