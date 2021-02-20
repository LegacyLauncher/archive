package ru.turikhay.tlauncher.user;

public final class MojangAuth extends MojangLikeAuth<MojangUser> {
    public MojangAuth() {
        super(MojangUser.FACTORY);
    }
}
