package com.turikhay.tlauncher.minecraft.auth;

public class AuthResponse extends RefreshResponse {
   private AuthResponse.User user;

   public AuthResponse.User getUser() {
      return this.user;
   }

   public class User {
      private String id;

      public String getId() {
         return this.id;
      }
   }
}
