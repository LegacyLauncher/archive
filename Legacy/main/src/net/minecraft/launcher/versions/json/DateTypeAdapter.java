package net.minecraft.launcher.versions.json;

import com.google.gson.*;
import ru.turikhay.exceptions.ParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTypeAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    private final DateFormat enUsFormat;
    private final DateFormat iso8601Format;

    public DateTypeAdapter() {
        enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ssZ");
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        } else {
            Date date = toDate(json.getAsString());
            if (typeOfT == Date.class) {
                return date;
            } else {
                throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
            }
        }
    }

    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        DateFormat var4 = enUsFormat;
        synchronized (enUsFormat) {
            return new JsonPrimitive(toString(src));
        }
    }

    public String toString(Date date) {
        DateFormat var2 = enUsFormat;
        synchronized (enUsFormat) {
            String result = iso8601Format.format(date);
            return result.substring(0, 22) + ":" + result.substring(22);
        }
    }

    public Date toDate(String string) {
        DateFormat var2 = enUsFormat;
        synchronized (enUsFormat) {
            Date var10000;
            try {
                var10000 = enUsFormat.parse(string);
            } catch (Exception var6) {
                try {
                    var10000 = iso8601Format.parse(string);
                } catch (Exception var5) {
                    try {
                        String e = string.replace("Z", "+00:00");
                        e = e.substring(0, 22) + e.substring(23);
                        var10000 = iso8601Format.parse(e);
                    } catch (Exception var4) {
                        throw new ParseException("Invalid date: " + string, var4);
                    }

                    return var10000;
                }

                return var10000;
            }

            return var10000;
        }
    }
}
