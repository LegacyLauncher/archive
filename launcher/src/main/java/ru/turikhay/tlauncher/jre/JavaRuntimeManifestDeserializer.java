package ru.turikhay.tlauncher.jre;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class JavaRuntimeManifestDeserializer implements JsonDeserializer<JavaRuntimeManifest> {
    @Override
    public JavaRuntimeManifest deserialize(JsonElement json, Type typeOfT,
                                           JsonDeserializationContext context) throws JsonParseException {
        ArrayList<JavaRuntimeManifest.RuntimeFile> files = new ArrayList<>();
        JsonObject object = json.getAsJsonObject();
        JsonObject filesObj = object.get("files").getAsJsonObject();
        for (Map.Entry<String, JsonElement> fileEntry : filesObj.entrySet()) {
            String path = fileEntry.getKey();
            JavaRuntimeManifest.RuntimeFile file = context.deserialize(fileEntry.getValue(),
                    JavaRuntimeManifest.RuntimeFile.class);
            file.setPath(path);
            files.add(file);
        }
        return new JavaRuntimeManifest(files);
    }
}
