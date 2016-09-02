package ru.turikhay.tlauncher.bootstrap.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Locale;

public final class Json {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Locale.class, new LocaleDeserializer())
            .registerTypeAdapter(Version.class, new VersionJsonizer())
            .create();

    public static <T> T parse(InputStream in, Type type) throws IOException, JsonSyntaxException {
        String result = IOUtils.toString(new InputStreamReader(in, U.UTF8));

        try {
            return get().fromJson(result, type);
        } catch (JsonSyntaxException jsE) {
            throw new RuntimeException(result, jsE);
        }
    }

    public static Gson get() {
        return GSON;
    }

    private Json() {
    }
}
