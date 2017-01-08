package ru.turikhay.tlauncher.bootstrap.json;

import shaded.com.google.gson.JsonDeserializationContext;
import shaded.com.google.gson.JsonDeserializer;
import shaded.com.google.gson.JsonElement;
import shaded.com.google.gson.JsonParseException;
import ru.turikhay.tlauncher.bootstrap.util.U;

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
