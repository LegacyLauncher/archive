package ru.turikhay.tlauncher.user;

import java.io.IOException;

public final class MojangAuth extends AuthlibAuth<MojangUser> implements StandardAuth<MojangUser> {

    public MojangUser authorize(String username, String password) throws AuthException, IOException {
        String clientToken = randomClientToken();
        com.mojang.authlib.UserAuthentication userAuthentication = super.authorize(clientToken, username, password);
        return new MojangUser(clientToken, username, userAuthentication);
    }

}
