package ru.turikhay.tlauncher.bootstrap.json;

import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.com.google.gson.*;

import java.lang.reflect.Type;

public class UpdateDeserializer implements JsonDeserializer<UpdateMeta> {
    @Override
    public UpdateMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        UpdateMeta meta = Json.get().fromJson(jsonElement, UpdateMeta.class);
        U.requireNotNull(meta.getLauncher(), "launcher entry");
        U.requireNotNull(meta.getBootstrap(), "bootstrap entry");
        
        JsonObject object = jsonElement.getAsJsonObject();

        String options;
        if(object.has("options")) {
            options = Json.get().toJson(object.get("options"));
        } else {
            options = null;
        }
        meta.setOptions(options);

        return meta;
    }
}
