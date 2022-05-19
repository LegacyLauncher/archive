package ru.turikhay.tlauncher.bootstrap.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static ru.turikhay.tlauncher.bootstrap.json.Json.parse;

public class RemoteMetaDeserializer {
    protected final String shortBrand;

    public RemoteMetaDeserializer(String shortBrand) {
        this.shortBrand = shortBrand;
    }

    protected static RemoteMeta parseRemoteMeta(JsonDeserializationContext ctx, JsonObject o) {
        return new RemoteMeta(
                parse(ctx, o, "version", Version.class),
                parse(ctx, o, "checksum", String.class),
                parse(ctx, o, "url", typeOfUrlList())
        );
    }

    protected static Type typeOfUrlList() {
        return new TypeToken<List<URL>>() {
        }.getType();
    }

    protected static Type typeOfStringMap() {
        return new TypeToken<Map<String, String>>() {
        }.getType();
    }

    protected static class RemoteMeta {
        final Version version;
        final String checksum;
        final List<URL> url;

        public RemoteMeta(Version version, String checksum, List<URL> url) {
            this.version = version;
            this.checksum = checksum;
            this.url = url;
        }
    }
}
