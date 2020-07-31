package ru.turikhay.tlauncher.user;

import com.mojang.authlib.UserAuthentication;

public class MojangUser extends AuthlibUser {
    public static final String TYPE = "mojang";

    MojangUser(String clientToken, String username, UserAuthentication userAuthentication) {
        super(clientToken, username, userAuthentication);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static MojangUserJsonizer getJsonizer() {
        return new MojangUserJsonizer(new MojangAuth());
    }
}
