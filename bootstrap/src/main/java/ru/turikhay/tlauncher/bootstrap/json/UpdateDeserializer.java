package ru.turikhay.tlauncher.bootstrap.json;

import com.google.gson.*;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.lang.reflect.Type;
import java.util.Map;

public class UpdateDeserializer implements JsonDeserializer<UpdateMeta> {
    @Override
    public UpdateMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        U.log("[UpdateDeserializer]", "Deserializing");

        UpdateMeta meta = Json.get().fromJson(jsonElement, UpdateMeta.class);
        U.requireNotNull(meta.getLauncher(), "launcher entry");
        U.requireNotNull(meta.getBootstrap(), "bootstrap entry");
        
        JsonObject object = jsonElement.getAsJsonObject();

        String options;
        if(object.has("options")) {
            JsonObject optionsObject = (JsonObject) object.get("options");
            if(object.has("java") && !optionsObject.has("java")) {
                optionsObject.add("java", object.get("java"));
            }
            options = Json.get().toJson(optionsObject);
        } else {
            options = null;
        }
        meta.setOptions(options);

        Map<String, String> description = meta.getDescription();
        if(description != null) {
            meta.getLauncher().setDescription(description);
        }

        return meta;
    }
}
