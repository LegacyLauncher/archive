package ru.turikhay.util.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;

import java.lang.reflect.Type;

public class VersionSerializer implements JsonSerializer<Version>, JsonDeserializer<Version> {
    @Override
    public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json == null ? null : Version.valueOf(json.getAsString());
    }

    @Override
    public JsonElement serialize(Version src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.toString());
    }
}
