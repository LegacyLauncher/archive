package net.legacylauncher.bootstrap.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;

import java.lang.reflect.Type;

public class VersionSerializer implements JsonSerializer<Version>, JsonDeserializer<Version> {
    @Override
    public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Version.parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(Version src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
