package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import ru.turikhay.util.U;

import java.util.HashMap;
import java.util.Map;

public abstract class AuthlibUserJsonizer<T extends AuthlibUser> extends UserJsonizer<T> {
    private final AuthlibAuth auth;

    AuthlibUserJsonizer(AuthlibAuth auth) {
        this.auth = U.requireNotNull(auth, "auth");
    }

    protected Map<String, Object> createStorage(T src) {
        Map<String, Object> storage = new HashMap<>(src.getMojangUserAuthentication().saveForStorage());
        storage.put("clientToken", src.getClientToken());
        storage.put("username", src.getUsername());
        return storage;
    }

    protected abstract T createFromUserAuthentication(com.mojang.authlib.UserAuthentication userAuthentication, JsonObject object, JsonDeserializationContext context);

    @Override
    public JsonObject serialize(T src, JsonSerializationContext context) {
        Map<String, Object> storage = createStorage(src);
        return (JsonObject) context.serialize(storage);
    }

    @Override
    public T deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String clientToken = json.get("clientToken").getAsString();

        Map<String, Object> storage = context.deserialize(json, new TypeToken<Map<String, Object>>(){}.getType());
        U.requireNotNull(storage, "storage");
        if(storage.isEmpty()) {
            throw new IllegalArgumentException("storage is empty");
        }

        com.mojang.authlib.UserAuthentication userAuthentication = auth.createUserAuthentication(clientToken);
        userAuthentication.loadFromStorage(storage);

        return createFromUserAuthentication(userAuthentication, json, context);
    }
}
