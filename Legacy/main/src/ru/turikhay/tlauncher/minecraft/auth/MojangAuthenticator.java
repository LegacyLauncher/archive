package ru.turikhay.tlauncher.minecraft.auth;

public class MojangAuthenticator extends StandardAuthenticator {
    private static final String AUTH_URL = "https://authserver.mojang.com/";

    public MojangAuthenticator(Account account) {
        super(account, "https://authserver.mojang.com/authenticate", "https://authserver.mojang.com/refresh");
    }
}
