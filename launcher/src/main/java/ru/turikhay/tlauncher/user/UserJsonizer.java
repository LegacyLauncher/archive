package ru.turikhay.tlauncher.user;

import com.google.gson.*;

import java.lang.reflect.Type;

abstract class UserJsonizer<T extends User> implements JsonSerializer<T>, JsonDeserializer<T> {
    public abstract JsonObject serialize(T src, JsonSerializationContext context);

    public abstract T deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException;

    @Override
    public final JsonObject serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src, context);
    }

    @Override
    public final T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json.getAsJsonObject(), context);
    }
}
