package com.turikhay.tlauncher.minecraft.auth;

public class AuthenticationRequest extends Request {
   private Agent agent;
   private String username;
   private String password;
   private String clientToken;
   private boolean requestUser;

   AuthenticationRequest(String username, String password, String clientToken) {
      this.agent = Agent.MINECRAFT;
      this.requestUser = true;
      this.username = username;
      this.password = password;
      this.clientToken = clientToken;
   }

   AuthenticationRequest(Authenticator auth) {
      this(auth.getUsername(), auth.getPassword(), auth.getClientToken());
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
      return this.requestUser;
   }
}
