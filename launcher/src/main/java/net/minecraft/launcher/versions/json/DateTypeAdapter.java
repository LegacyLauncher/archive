package net.minecraft.launcher.versions.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

public class DateTypeAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    private final boolean returnEpochIfInvalid;

    public DateTypeAdapter(boolean returnEpochIfInvalid) {
        this.returnEpochIfInvalid = returnEpochIfInvalid;
    }

    public Date parse(String value) {
        try {
            return Date.from(OffsetDateTime.parse(value).toInstant());
        } catch (RuntimeException e) {
            if (returnEpochIfInvalid) {
                return Date.from(Instant.EPOCH);
            }
            throw e;
        }
    }

    public String format(Date date) {
        return date.toInstant().toString();
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("date must be a string");
        }
        return parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(format(src));
    }
}
