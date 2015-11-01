package ru.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import net.minecraft.launcher.Http;
import org.apache.commons.lang3.StringUtils;

public class StandardAuthenticator extends Authenticator {
   protected final Gson gson = (new GsonBuilder()).registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
   protected final URL AUTHENTICATE_URL;
   protected final URL REFRESH_URL;

   protected StandardAuthenticator(Account account, String authUrl, String refreshUrl) {
      super(account);
      this.AUTHENTICATE_URL = Http.constantURL(authUrl);
      this.REFRESH_URL = Http.constantURL(refreshUrl);
   }

   protected void pass() throws AuthenticatorException {
      if (this.account.isFree()) {
         throw new IllegalArgumentException("invalid account type");
      } else if (this.account.getPassword() == null && this.account.getAccessToken() == null) {
         throw new AuthenticatorException(new NullPointerException("password/accessToken"));
      } else {
         this.log(new Object[]{this.account});
         this.log(new Object[]{"hasUsername:", this.account.getUsername()});
         this.log(new Object[]{"hasPassword:", this.account.getPassword() != null});
         this.log(new Object[]{"hasAccessToken:", this.account.getAccessToken() != null});
         if (this.account.getPassword() == null) {
            this.tokenLogin();
         } else {
            this.passwordLogin();
         }

         this.log(new Object[]{"Log in successful!"});
         this.log(new Object[]{"hasUUID:", this.account.getUUID() != null});
         this.log(new Object[]{"hasAccessToken:", this.account.getAccessToken() != null});
         this.log(new Object[]{"hasProfiles:", this.account.getProfiles() != null});
         this.log(new Object[]{"hasProfile:", this.account.getProfiles() != null});
         this.log(new Object[]{"hasProperties:", this.account.getProperties() != null});
         this.log(new Object[]{this.account.getProfile()});
      }
   }

   protected void passwordLogin() throws AuthenticatorException {
      this.log(new Object[]{"Loggining in with password"});
      StandardAuthenticator.AuthenticationRequest request = new StandardAuthenticator.AuthenticationRequest(this);
      StandardAuthenticator.AuthenticationResponse response = (StandardAuthenticator.AuthenticationResponse)this.makeRequest(this.AUTHENTICATE_URL, request, StandardAuthenticator.AuthenticationResponse.class);
      this.account.setUserID(response.getUserID() != null ? response.getUserID() : this.account.getUsername());
      this.account.setAccessToken(response.getAccessToken());
      this.account.setPassword((String)null);
      this.account.setProfiles(response.getAvailableProfiles());
      this.account.setProfile(response.getSelectedProfile());
      this.account.setUser(response.getUser());
      setClientToken(response.getClientToken());
      if (response.getSelectedProfile() != null) {
         this.account.setUUID(response.getSelectedProfile().getId());
         this.account.setDisplayName(response.getSelectedProfile().getName());
      }

   }

   protected void tokenLogin() throws AuthenticatorException {
      this.log(new Object[]{"Loggining in with token"});
      StandardAuthenticator.RefreshRequest request = new StandardAuthenticator.RefreshRequest(this);
      StandardAuthenticator.RefreshResponse response = (StandardAuthenticator.RefreshResponse)this.makeRequest(this.REFRESH_URL, request, StandardAuthenticator.RefreshResponse.class);
      this.account.setAccessToken(response.getAccessToken());
      this.account.setProfile(response.getSelectedProfile());
      this.account.setUser(response.getUser());
      setClientToken(response.getClientToken());
      if (response.getSelectedProfile() != null) {
         this.account.setUUID(response.getSelectedProfile().getId());
         this.account.setDisplayName(response.getSelectedProfile().getName());
      }

   }

   protected StandardAuthenticator.Response makeRequest(URL url, StandardAuthenticator.Request input, Class classOfT) throws AuthenticatorException {
      if (url == null) {
         throw new NullPointerException("url");
      } else {
         String jsonResult;
         try {
            if (input == null) {
               jsonResult = AuthenticatorService.performGetRequest(url);
            } else {
               jsonResult = AuthenticatorService.performPostRequest(url, this.gson.toJson((Object)input), "application/json");
            }
         } catch (IOException var8) {
            throw new AuthenticatorException("Error making request, uncaught IOException", "unreachable", var8);
         }

         StandardAuthenticator.Response result;
         try {
            result = (StandardAuthenticator.Response)this.gson.fromJson(jsonResult, classOfT);
         } catch (RuntimeException var7) {
            throw new AuthenticatorException("Error parsing response: \"" + jsonResult + "\"", "unparseable", var7);
         }

         if (result == null) {
            return null;
         } else if (StringUtils.isBlank(result.getError())) {
            return result;
         } else {
            throw this.getException(result);
         }
      }
   }

   protected AuthenticatorException getException(StandardAuthenticator.Response result) {
      return (AuthenticatorException)("UserMigratedException".equals(result.getCause()) ? new UserMigratedException() : ("ForbiddenOperationException".equals(result.getError()) ? new InvalidCredentialsException() : new AuthenticatorException(result, "internal")));
   }

   protected static class Response {
      private String error;
      private String errorMessage;
      private String cause;

      public String getError() {
         return this.error;
      }

      public String getCause() {
         return this.cause;
      }

      public String getErrorMessage() {
         return this.errorMessage;
      }
   }

   protected static class Request {
   }

   protected static class RefreshResponse extends StandardAuthenticator.Response {
      private String accessToken;
      private String clientToken;
      private GameProfile selectedProfile;
      private User user;

      public String getAccessToken() {
         return this.accessToken;
      }

      public String getClientToken() {
         return this.clientToken;
      }

      public GameProfile getSelectedProfile() {
         return this.selectedProfile;
      }

      public User getUser() {
         return this.user;
      }
   }

   protected static class RefreshRequest extends StandardAuthenticator.Request {
      private String clientToken;
      private String accessToken;
      private GameProfile selectedProfile;
      private boolean requestUser;

      private RefreshRequest(String clientToken, String accessToken, GameProfile profile) {
         this.requestUser = true;
         this.clientToken = clientToken;
         this.accessToken = accessToken;
         this.selectedProfile = profile;
      }

      RefreshRequest(String clientToken, String accessToken) {
         this(clientToken, accessToken, (GameProfile)null);
      }

      private RefreshRequest(Authenticator auth, GameProfile profile) {
         this(Authenticator.getClientToken().toString(), auth.account.getAccessToken(), profile);
      }

      RefreshRequest(Authenticator auth) {
         this((Authenticator)auth, (GameProfile)null);
      }

      public String getClientToken() {
         return this.clientToken;
      }

      public String getAccessToken() {
         return this.accessToken;
      }

      public GameProfile getProfile() {
         return this.selectedProfile;
      }
   }

   protected static class AuthenticationResponse extends StandardAuthenticator.Response {
      private String accessToken;
      private String clientToken;
      private GameProfile selectedProfile;
      private GameProfile[] availableProfiles;
      private User user;

      public String getAccessToken() {
         return this.accessToken;
      }

      public String getClientToken() {
         return this.clientToken;
      }

      public GameProfile[] getAvailableProfiles() {
         return this.availableProfiles;
      }

      public GameProfile getSelectedProfile() {
         return this.selectedProfile;
      }

      public User getUser() {
         return this.user;
      }

      public String getUserID() {
         return this.user != null ? this.user.getID() : null;
      }
   }

   protected static class AuthenticationRequest extends StandardAuthenticator.Request {
      private Agent agent;
      private String username;
      private String password;
      private String clientToken;

      protected AuthenticationRequest(String username, String password, String clientToken) {
         this.agent = Agent.MINECRAFT;
         this.username = username;
         this.password = password;
         this.clientToken = clientToken;
      }

      protected AuthenticationRequest(Authenticator auth) {
         this(auth.account.getUsername(), auth.account.getPassword(), Authenticator.getClientToken().toString());
      }

      public Agent getAgent() {
         return this.agent;
      }

      public String getUsername() {
         return this.username;
      }

      public String getPassword() {
         return this.password;
      }

      public String getClientToken() {
         return this.clientToken;
      }

      public boolean isRequestingUser() {
         return true;
      }
   }
}
