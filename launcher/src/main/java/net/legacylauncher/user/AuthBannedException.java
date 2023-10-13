package net.legacylauncher.user;

public class AuthBannedException extends AuthException {
    public AuthBannedException() {
        super((String) null, "banned");
    }
}
