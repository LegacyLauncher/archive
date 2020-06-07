package ru.turikhay.tlauncher.minecraft;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.U;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class PromotedServerDeserializer implements JsonDeserializer<PromotedServer> {

    @Override
    public PromotedServer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        PromotedServer server = new PromotedServer();
        server.setName(requireString(object, "name"));
        server.setFullAddress(requireString(object, "address"));

        if(object.get("family").isJsonArray()) {
            server.getFamily().addAll((List<String>) context.deserialize(object.get("family"), new TypeToken<List<String>>(){}.getRawType()));
        } else {
            server.getFamily().add(getString(object, "family"));
        }

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
