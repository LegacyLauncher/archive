package com.turikhay.tlauncher.minecraft.auth;

public class RefreshRequest extends Request {
   private String clientToken;
   private String accessToken;
   private GameProfile selectedProfile;
   private boolean requestUser;

   RefreshRequest(String clientToken, String accessToken, GameProfile profile) {
      this.requestUser = true;
      this.clientToken = clientToken;
      this.accessToken = accessToken;
      this.selectedProfile = profile;
   }

   RefreshRequest(String clientToken, String accessToken) {
      this(clientToken, accessToken, (GameProfile)null);
   }

   RefreshRequest(Authenticator auth, GameProfile profile) {
      this(auth.getClientToken().toString(), auth.account.getAccessToken(), profile);
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
