package ru.turikhay.tlauncher.user;

import com.mojang.authlib.UserAuthentication;

public class AuthlibUserPayload {
    private final String clientToken;
    private final String username;
    private final UserAuthentication authentication;

    public AuthlibUserPayload(String clientToken, String username, UserAuthentication authentication) {
        this.clientToken = clientToken;
        this.username = username;
        this.authentication = authentication;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getUsername() {
        return username;
    }

    public UserAuthentication getAuthentication() {
        return authentication;
    }
}
