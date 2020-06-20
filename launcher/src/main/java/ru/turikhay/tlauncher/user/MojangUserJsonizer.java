package ru.turikhay.tlauncher.user;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.UserAuthentication;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MojangUserJsonizer extends AuthlibUserJsonizer<MojangUser> {

    MojangUserJsonizer(MojangAuth auth) {
        super(auth);
    }

    @Override
    protected MojangUser createFromUserAuthentication(UserAuthentication userAuthentication, JsonObject object, JsonDeserializationContext context) {
        return new MojangUser(object.get("clientToken").getAsString(), object.get("username").getAsString(), userAuthentication);
    }
}
