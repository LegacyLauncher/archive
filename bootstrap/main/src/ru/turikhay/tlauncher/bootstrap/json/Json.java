package ru.turikhay.tlauncher.bootstrap.json;

import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import shaded.com.github.zafarkhaja.semver.Version;
import shaded.com.google.gson.Gson;
import shaded.com.google.gson.GsonBuilder;
import shaded.com.google.gson.JsonSyntaxException;
import shaded.org.apache.commons.io.IOUtils;
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

    private Json() {
        throw new NoSuchMethodError();
    }
}
