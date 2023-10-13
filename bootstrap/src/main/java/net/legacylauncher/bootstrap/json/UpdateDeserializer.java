package net.legacylauncher.bootstrap.json;

import com.google.gson.*;
import net.legacylauncher.bootstrap.meta.RemoteBootstrapMeta;
import net.legacylauncher.bootstrap.meta.RemoteLauncherMeta;
import net.legacylauncher.bootstrap.meta.UpdateMeta;
import net.legacylauncher.bootstrap.util.U;

import java.lang.reflect.Type;

public class UpdateDeserializer implements JsonDeserializer<UpdateMeta> {
    @Override
    public UpdateMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        U.log("[UpdateDeserializer]", "Deserializing");
        JsonObject object = jsonElement.getAsJsonObject();
        return new UpdateMeta(
                Json.parse(ctx, object, "bootstrap_java", RemoteBootstrapMeta.class, false),
                Json.parse(ctx, object, "launcher", RemoteLauncherMeta.class, false),
                Json.parse(ctx, object, "launcher_beta", RemoteLauncherMeta.class, false),
                parseOptions(object)
        );
    }

    private static String parseOptions(JsonObject object) {
        String options;
        if (object.has("options")) {
            JsonObject optionsObject = (JsonObject) object.get("options");
            if (object.has("java") && !optionsObject.has("java")) {
                optionsObject.add("java", object.get("java"));
            }
            options = Json.get().toJson(optionsObject);
        } else {
            options = null;
        }
        return options;
    }
}
