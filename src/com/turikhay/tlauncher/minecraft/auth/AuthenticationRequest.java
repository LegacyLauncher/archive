package com.turikhay.tlauncher.minecraft.auth;

class AuthenticationRequest extends Request {
   private Agent agent;
   private String username;
   private String password;
   private String clientToken;

   private AuthenticationRequest(String username, String password, String clientToken) {
      this.agent = Agent.MINECRAFT;
      this.username = username;
      this.password = password;
      this.clientToken = clientToken;
   }

   AuthenticationRequest(Authenticator auth) {
      this(auth.account.getUsername(), auth.account.getPassword(), auth.getClientToken().toString());
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
      boolean requestUser = true;
      return requestUser;
   }
}
