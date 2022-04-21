package ru.turikhay.tlauncher.user;

import com.mojang.authlib.exceptions.AuthenticationException;

import java.io.IOException;

public final class MojangAuth extends MojangLikeAuth<MojangUser> {
    public MojangAuth() {
        super(MojangUser.FACTORY);
    }

    public MojangUser authorize(String username, String password) throws AuthException, IOException {
        try {
            return super.authorize(username, password);
        } catch (AuthUnknownException e) {
            if (e.getCause() instanceof AuthenticationException) {
                AuthenticationException ae = (AuthenticationException) e.getCause();
                if (MojangAccountMigratedException.detect(ae)) {
                    throw new MojangAccountMigratedException();
                }
            }
            throw e;
        }
    }
}
