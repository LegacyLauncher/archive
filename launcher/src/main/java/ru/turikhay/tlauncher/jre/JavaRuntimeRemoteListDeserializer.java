package ru.turikhay.tlauncher.jre;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JavaRuntimeRemoteListDeserializer implements JsonDeserializer<JavaRuntimeRemoteList> {
    @Override
    public JavaRuntimeRemoteList deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, JavaRuntimeRemoteList.Platform> perPlatform = new HashMap<>();
        JsonObject object = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> platformEntry : object.entrySet()) {
            String platformName = platformEntry.getKey();
            JsonObject platformObject = platformEntry.getValue().getAsJsonObject();
            ArrayList<JavaRuntimeRemote> runtimeList = new ArrayList<>();
            for (Map.Entry<String, JsonElement> platformObjectEntry : platformObject.entrySet()) {
                String runtimeName = platformObjectEntry.getKey();
                for (JsonElement runtime : platformObjectEntry.getValue().getAsJsonArray()) {
                    runtimeList.add(deserializeRuntimeEntry(runtimeName, platformName,
                            runtime.getAsJsonObject(), context));
                }
            }
            perPlatform.put(platformName, new JavaRuntimeRemoteList.Platform(platformName, runtimeList));
        }
        return new JavaRuntimeRemoteList(perPlatform);
    }

    private JavaRuntimeRemote deserializeRuntimeEntry(String name, String platform,
                                                      JsonObject object, JsonDeserializationContext context)
            throws JsonParseException {
        JavaRuntimeRemote result = context.deserialize(object, JavaRuntimeRemote.class);
        result.setName(name);
        result.setPlatform(platform);
        return result;
    }
}
