package ru.turikhay.tlauncher.user;

public class AuthBannedException extends AuthException {
    public AuthBannedException() {
        super((String) null, "banned");
    }
}
