package ru.turikhay.tlauncher.bootstrap.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Locale;

public final class Json {
    private static final Gson GSON = build().create();

    public static <T> T parse(InputStream in, Type type) throws IOException, JsonSyntaxException {
        return get().fromJson(new InputStreamReader(in, U.UTF8), type);
    }

    public static Gson get() {
        return GSON;
    }

    public static GsonBuilder build() {
        return ExposeExclusion.setup(new GsonBuilder())
                .registerTypeAdapter(Locale.class, new LocaleDeserializer())
                .registerTypeAdapter(Version.class, new VersionJsonizer());
    }

    public static JsonElement require(JsonObject o, String key) {
        if(o.has(key)) {
            return o.get(key);
        } else {
            throw new JsonParseException("missing required key \""+ key +"\"");
        }
    }

    public static <T> T parse(JsonDeserializationContext ctx, JsonObject o, String key, Type type) {
        T value;
        if(o.has(key)) {
            try {
                value = Validate.notNull((T) ctx.deserialize(o.get(key), type), key);
            } catch(RuntimeException rE) {
                throw new JsonParseException("error deserializing key \""+ key +"\" in " + o, rE);
            }
        } else {
            throw new JsonParseException("required key: \""+ key +"\"");
        }
        return value;
    }

    private Json() {
        throw new NoSuchMethodError();
    }
}
