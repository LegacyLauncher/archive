package net.legacylauncher.bootstrap.json;

import com.google.gson.*;
import net.legacylauncher.bootstrap.meta.RemoteLauncherMeta;

import java.lang.reflect.Type;
import java.util.Map;

import static net.legacylauncher.bootstrap.json.Json.parse;

public class RemoteLauncherDeserializer extends RemoteMetaDeserializer implements JsonDeserializer<RemoteLauncherMeta> {

    public RemoteLauncherDeserializer(String shortBrand) {
        super(shortBrand);
    }

    @Override
    public RemoteLauncherMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();
        RemoteMeta r = parseRemoteMeta(ctx, o);
        Map<String, String> description = parse(ctx, o, "description", typeOfStringMap(), false);
        return new RemoteLauncherMeta(r.version, shortBrand, r.checksum, r.url, description);
    }
}
