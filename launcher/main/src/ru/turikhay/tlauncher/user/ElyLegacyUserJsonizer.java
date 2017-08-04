package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;

public class ElyLegacyUserJsonizer extends UserJsonizer<ElyLegacyUser> {
    @Override
    public JsonObject serialize(ElyLegacyUser src, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("username", src.getUsername());
        object.addProperty("uuid", src.getUUID().toString());
        object.addProperty("displayName", src.getDisplayName());
        object.addProperty("clientToken", src.getClientToken());
        object.addProperty("accessToken", src.getAccessToken());
        return object;
    }

    @Override
    public ElyLegacyUser deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new ElyLegacyUser(
                json.get("username").getAsString(),
                UUIDTypeAdapter.fromString(json.get("uuid").getAsString()),
                json.get("displayName").getAsString(),
                json.get("clientToken").getAsString(),
                json.get("accessToken").getAsString()
        );
    }
}
