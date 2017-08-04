package ru.turikhay.tlauncher.user;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MojangUserJsonizer extends UserJsonizer<MojangUser> {
    @Override
    public JsonObject serialize(MojangUser src, JsonSerializationContext context) {
        Map<String, Object> storage = new HashMap<String, Object>(src.getMojangUserAuthentication().saveForStorage());
        storage.put("clientToken", src.getClientToken());
        storage.put("username", src.getUsername());
        return (JsonObject) context.serialize(storage);
    }

    @Override
    public MojangUser deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String
                clientToken = json.get("clientToken").getAsString(),
                username = json.get("username").getAsString();

        Map<String, Object> storage = context.deserialize(json, new TypeToken<Map<String, Object>>(){}.getType());
        U.requireNotNull(storage, "storage");
        if(storage.isEmpty()) {
            throw new IllegalArgumentException("storage is empty");
        }

        com.mojang.authlib.UserAuthentication userAuthentication = MojangAuth.createUserAuthentication(clientToken);
        userAuthentication.loadFromStorage(storage);

        return new MojangUser(clientToken, username, userAuthentication);
    }
}
