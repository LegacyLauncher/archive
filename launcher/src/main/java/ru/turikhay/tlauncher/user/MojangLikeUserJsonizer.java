package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.authlib.UserAuthentication;

public class MojangLikeUserJsonizer<M extends MojangLikeUser> extends AuthlibUserJsonizer<M> {
    private final MojangLikeUserFactory<M> userFactory;

    MojangLikeUserJsonizer(AuthlibAuth<M> auth, MojangLikeUserFactory<M> userFactory) {
        super(auth);
        this.userFactory = userFactory;
    }

    @Override
    protected M createFromUserAuthentication(UserAuthentication userAuthentication, JsonObject object, JsonDeserializationContext context) {
        return userFactory.createFromPayload(new AuthlibUserPayload(
                object.get("clientToken").getAsString(),
                object.get("username").getAsString(),
                userAuthentication
        ));
    }
}
