package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AuthlibUserJsonizer<T extends AuthlibUser> extends UserJsonizer<T> {
    private final AuthlibAuth<? extends AuthlibUser> auth;

    AuthlibUserJsonizer(AuthlibAuth<? extends AuthlibUser> auth) {
        this.auth = Objects.requireNonNull(auth, "auth");
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

        Map<String, Object> storage = context.deserialize(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        Objects.requireNonNull(storage, "storage");
        if (storage.isEmpty()) {
            throw new IllegalArgumentException("storage is empty");
        }

        com.mojang.authlib.UserAuthentication userAuthentication = auth.createUserAuthentication(clientToken);
        userAuthentication.loadFromStorage(storage);

        return createFromUserAuthentication(userAuthentication, json, context);
    }
}
