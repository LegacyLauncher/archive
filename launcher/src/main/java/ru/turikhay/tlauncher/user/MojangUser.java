package ru.turikhay.tlauncher.user;

public class MojangUser extends MojangLikeUser {
    public static final String TYPE = "mojang";
    public static final MojangLikeUserFactory<MojangUser> FACTORY = MojangUser::new;

    MojangUser(AuthlibUserPayload payload) {
        super(payload);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static MojangLikeUserJsonizer<MojangUser> getJsonizer() {
        return new MojangLikeUserJsonizer<>(new MojangAuth(), FACTORY);
    }
}
