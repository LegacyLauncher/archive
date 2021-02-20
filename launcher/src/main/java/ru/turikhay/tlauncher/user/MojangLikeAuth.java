package ru.turikhay.tlauncher.user;

import java.io.IOException;

public class MojangLikeAuth<M extends MojangLikeUser> extends AuthlibAuth<M> implements StandardAuth<M> {
    private final MojangLikeUserFactory<M> factory;

    public MojangLikeAuth(MojangLikeUserFactory<M> factory) {
        this.factory = factory;
    }

    public M authorize(String username, String password) throws AuthException, IOException {
        String clientToken = randomClientToken();
        com.mojang.authlib.UserAuthentication userAuthentication = super.authorize(clientToken, username, password);
        return factory.createFromPayload(new AuthlibUserPayload(clientToken, username, userAuthentication));
    }

}