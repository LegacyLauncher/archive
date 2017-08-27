package ru.turikhay.tlauncher.user;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.util.UUID;

public final class MojangAuth extends AuthlibAuth<MojangUser> implements StandardAuth<MojangUser> {

    public MojangUser authorize(String username, String password) throws AuthException, IOException {
        String clientToken = randomClientToken();
        com.mojang.authlib.UserAuthentication userAuthentication = super.authorize(clientToken, username, password);
        return new MojangUser(clientToken, username, userAuthentication);
    }

}
