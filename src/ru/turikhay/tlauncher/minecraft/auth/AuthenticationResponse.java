package ru.turikhay.tlauncher.minecraft.auth;

class AuthenticationResponse extends Response {
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
