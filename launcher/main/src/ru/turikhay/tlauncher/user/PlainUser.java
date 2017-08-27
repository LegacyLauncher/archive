package ru.turikhay.tlauncher.user;

import com.google.gson.*;
import ru.turikhay.util.StringUtil;

import java.lang.reflect.Type;
import java.util.UUID;

public class PlainUser extends User {
    public static final String TYPE = "plain";
    private final String username;

    PlainUser(String username) {
        this.username = StringUtil.requireNotBlank(username, "username");
    }

    @Override
    public String getUsername() {
        return username;
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
                "null",
                null,
                username,
                new UUID(0, 0),
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
            return new PlainUser(json.get("username").getAsString());
        }

        @Override
        public JsonObject serialize(PlainUser src, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("username", src.getUsername());
            return object;
        }
    }
}
