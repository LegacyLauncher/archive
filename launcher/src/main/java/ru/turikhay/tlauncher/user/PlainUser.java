package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.StringUtil;

import java.util.Objects;
import java.util.UUID;

public class PlainUser extends User {
    public static final String TYPE = "plain";
    private final String username;
    private final UUID uuid;

    public PlainUser(String username, UUID uuid) {
        this.username = StringUtil.requireNotBlank(username, "username");
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return username;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected boolean equals(User user) {
        return user != null && getUsername().equalsIgnoreCase(user.getUsername());
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + TYPE.hashCode();
        return result;
    }

    @Override
    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(username,
                UUIDTypeAdapter.fromUUID(uuid),
                null,
                username,
                uuid,
                "legacy",
                "(Default)"
        );
    }

    public static UserJsonizer<PlainUser> getJsonizer() {
        return new Jsonizer();
    }

    private static class Jsonizer extends UserJsonizer<PlainUser> {
        @Override
        public PlainUser deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
            UUID uuid;
            if (json.has("uuid")) {
                uuid = UUIDTypeAdapter.fromString(json.get("uuid").getAsString());
            } else {
                uuid = UUID.randomUUID();
            }
            return new PlainUser(json.get("username").getAsString(), uuid);
        }

        @Override
        public JsonObject serialize(PlainUser src, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("username", src.getUsername());
            object.addProperty("uuid", UUIDTypeAdapter.fromUUID(src.getUUID()));
            return object;
        }
    }
}
