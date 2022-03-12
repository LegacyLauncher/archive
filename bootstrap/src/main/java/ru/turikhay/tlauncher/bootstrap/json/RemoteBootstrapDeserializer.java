package ru.turikhay.tlauncher.bootstrap.json;

import com.google.gson.*;
import ru.turikhay.tlauncher.bootstrap.meta.DownloadEntry;
import ru.turikhay.tlauncher.bootstrap.meta.RemoteBootstrapMeta;

import java.lang.reflect.Type;

public class RemoteBootstrapDeserializer extends RemoteMetaDeserializer implements JsonDeserializer<RemoteBootstrapMeta> {

    public RemoteBootstrapDeserializer(String shortBrand) {
        super(shortBrand);
    }

    @Override
    public RemoteBootstrapMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();
        RemoteMeta r = parseRemoteMeta(ctx, o);
        return new RemoteBootstrapMeta(
                r.version,
                shortBrand,
                new DownloadEntry("bootstrap:" + shortBrand, r.url, r.checksum)
        );
    }
}
