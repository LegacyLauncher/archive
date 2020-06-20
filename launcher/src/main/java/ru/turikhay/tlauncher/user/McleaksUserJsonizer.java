package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.authlib.UserAuthentication;

import java.util.Map;

public class McleaksUserJsonizer extends AuthlibUserJsonizer<McleaksUser> {

    McleaksUserJsonizer(McleaksAuth auth) {
        super(auth);
    }

    protected Map<String, Object> createStorage(McleaksUser src) {
        Map<String, Object> storage = super.createStorage(src);
        storage.put("altToken", src.getAltToken());
        return storage;
    }

    @Override
    protected McleaksUser createFromUserAuthentication(UserAuthentication userAuthentication, JsonObject object, JsonDeserializationContext context) {
        return new McleaksUser(object.get("altToken").getAsString(), object.get("clientToken").getAsString(), userAuthentication);
    }
}
