package ru.turikhay.tlauncher.minecraft;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.U;

import java.lang.reflect.Type;
import java.util.Set;

public class ServerDeserializer implements JsonDeserializer<Server> {

    @Override
    public Server deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        Server server = new Server();
        server.setName(requireString(object, "name"));
        server.setFullAddress(requireString(object, "address"));
        server.setFamily(getString(object, "family"));

        if(object.has("accounts")) {
            Set<Account.AccountType> accountTypes = context.deserialize(object.get("accounts"), new TypeToken<Set<Account.AccountType>>(){}.getRawType());
            server.getAccountTypeSet().clear();
            server.getAccountTypeSet().addAll(accountTypes);
            U.requireNotContainNull(server.getAccountTypeSet(), "accounts");
        }

        return server;
    }

    private String requireString(JsonObject object, String key) {
        return U.requireNotNull(getString(object, key), key);
    }

    private String getString(JsonObject object, String key) {
        JsonElement value = U.requireNotNull(object, "object").get(key);
        if(value != null && value.isJsonPrimitive() && ((JsonPrimitive) value).isString()) {
            return value.getAsString();
        }
        return null;
    }

}
