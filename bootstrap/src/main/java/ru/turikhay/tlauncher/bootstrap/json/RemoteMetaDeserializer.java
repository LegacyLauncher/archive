package ru.turikhay.tlauncher.bootstrap.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RemoteMetaDeserializer {
    protected final String shortBrand;

    public RemoteMetaDeserializer(String shortBrand) {
        this.shortBrand = shortBrand;
    }

    protected static RemoteMeta parseRemoteMeta(JsonDeserializationContext ctx, JsonObject o) {
        return new RemoteMeta(
                parse(ctx, o, "version", Version.class),
                parse(ctx, o, "checksum", String.class),
                (List<URL>) parse(ctx, o, "url", typeOfUrlList())
        );
    }

    protected static <T> T parse(JsonDeserializationContext ctx, JsonObject o, String key, Class<T> type) {
        return parse(ctx, o, key, (Type) type);
    }

    protected static <T> T parse(JsonDeserializationContext ctx, JsonObject o, String key, Type type) {
        return Json.parse(ctx, o, key, type);
    }

    protected static Type typeOfUrlList() {
        return new TypeToken<List<URL>>() {}.getType();
    }

    protected static Type typeOfStringMap() {
        return new TypeToken<Map<String, String>>() {}.getType();
    }

    protected static class RemoteMeta {
        Version version;
        String checksum;
        List<URL> url;

        public RemoteMeta(Version version, String checksum, List<URL> url) {
            this.version = version;
            this.checksum = checksum;
            this.url = url;
        }
    }
}