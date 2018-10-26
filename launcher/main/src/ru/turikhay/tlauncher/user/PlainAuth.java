package ru.turikhay.tlauncher.user;

import java.util.UUID;

public final class PlainAuth implements Auth<PlainUser> {

    public PlainUser authorize(String username) {
        return new PlainUser(username, UUID.randomUUID());
    }

    @Override
    public void validate(PlainUser user) {
        // always valid
    }

}
