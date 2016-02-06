package ru.turikhay.tlauncher.minecraft.auth;

public class MojangAuthenticator extends StandardAuthenticator {
   public MojangAuthenticator(Account account) {
      super(account, "https://authserver.mojang.com/authenticate", "https://authserver.mojang.com/refresh");
   }
}
