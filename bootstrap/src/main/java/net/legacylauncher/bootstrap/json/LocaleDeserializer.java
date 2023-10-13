package net.legacylauncher.bootstrap.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.legacylauncher.bootstrap.util.U;

import java.lang.reflect.Type;
import java.util.Locale;

public class LocaleDeserializer implements JsonDeserializer<Locale> {
    @Override
    public Locale deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String str = json.getAsString();
        Locale locale = U.getLocale(str);

        if (locale == null) {
            throw new NullPointerException();
        }

        if ("und".equals(locale.toString())) {
            throw new JsonParseException("can't find locale: " + str);
        }

        return locale;
    }
}
