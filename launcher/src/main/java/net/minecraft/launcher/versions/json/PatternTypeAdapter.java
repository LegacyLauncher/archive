package net.minecraft.launcher.versions.json;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class PatternTypeAdapter implements JsonSerializer<Pattern>, JsonDeserializer<Pattern> {
    public Pattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) return null;
        String string = json.getAsString();
        return StringUtils.isBlank(string) ? null : Pattern.compile(string);
    }

    public JsonElement serialize(Pattern src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
    }
}
