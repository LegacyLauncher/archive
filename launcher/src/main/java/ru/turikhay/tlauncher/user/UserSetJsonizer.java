package ru.turikhay.tlauncher.user;

import com.google.gson.*;
import ru.turikhay.tlauncher.managed.ManagedListener;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserSetJsonizer implements JsonDeserializer<UserSet>, JsonSerializer<UserSet> {
    private final Map<String, UserJsonizer<?>> jsonizerMap;
    private final ManagedListener<User> listener;

    public UserSetJsonizer(ManagedListener<User> listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.jsonizerMap = new HashMap<>();
        this.jsonizerMap.put(MojangUser.TYPE, MojangUser.getJsonizer());
        this.jsonizerMap.put(MinecraftUser.TYPE, MinecraftUser.getJsonizer());
        this.jsonizerMap.put(PlainUser.TYPE, PlainUser.getJsonizer());
        this.jsonizerMap.put(ElyUser.TYPE, ElyUser.getJsonizer());
        this.jsonizerMap.put(ElyLegacyUser.TYPE, ElyLegacyUser.getJsonizer());
        this.jsonizerMap.put(McleaksUser.TYPE, McleaksUser.getJsonizer());
    }

    @Override
    public UserSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        UserSet set = new UserSet(listener);

        JsonArray array = root.getAsJsonArray("list");
        for (JsonElement elem : array) {
            set.add(deserializeUser(elem, context));
        }

        if (root.has("selected")) {
            SelectedUser selectedUserRaw = context.deserialize(root.getAsJsonObject("selected"), SelectedUser.class);
            User selectedUser = set.getByUsername(selectedUserRaw.username, selectedUserRaw.type);
            set.select(selectedUser);
        }

        return set;
    }

    private User deserializeUser(JsonElement json, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();
        String type = object.getAsJsonPrimitive("type").getAsString();

        UserJsonizer<?> jsonizer = jsonizerMap.get(type);
        if (jsonizer == null) {
            throw new IllegalArgumentException("could not find jsonizer: " + type);
        }

        return jsonizer.deserialize(object, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(UserSet src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        if (src.getSelected() != null) {
            root.add("selected", context.serialize(new SelectedUser(src.getSelected())));
        }

        JsonArray array = new JsonArray();

        for (User user : src.getSet()) {
            String type = user.getType();
            UserJsonizer<User> jsonizer = (UserJsonizer<User>) jsonizerMap.get(type);

            if (jsonizer == null) {
                throw new IllegalArgumentException("jsonizer not found: " + type);
            }

            JsonObject userObject = jsonizer.serialize(user, context);
            if (userObject.has("type")) {
                throw new IllegalArgumentException("serialized object already has \"type\"");
            }
            userObject.addProperty("type", type);
            array.add(userObject);
        }
        root.add("list", array);

        return root;
    }

    private static class SelectedUser {
        private String username, type;

        public SelectedUser() {
        }

        public SelectedUser(User user) {
            if (user == null) {
                return;
            }

            this.username = user.getUsername();
            this.type = user.getType();
        }
    }
}
