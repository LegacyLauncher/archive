package ru.turikhay.tlauncher.user;

public final class PlainAuth implements Auth<PlainUser> {

    public PlainUser authorize(String username) {
        return new PlainUser(username);
    }

    @Override
    public void validate(PlainUser user) {
        // always valid
    }

}
