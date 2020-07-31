package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.authlib.UserAuthentication;

public class MojangUserJsonizer extends AuthlibUserJsonizer<MojangUser> {

    MojangUserJsonizer(MojangAuth auth) {
        super(auth);
    }

    @Override
    protected MojangUser createFromUserAuthentication(UserAuthentication userAuthentication, JsonObject object, JsonDeserializationContext context) {
        return new MojangUser(object.get("clientToken").getAsString(), object.get("username").getAsString(), userAuthentication);
    }
}
