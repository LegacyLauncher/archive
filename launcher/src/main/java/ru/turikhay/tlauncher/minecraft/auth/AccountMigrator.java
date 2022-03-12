package ru.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.properties.PropertyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.user.ElyLegacyUser;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.User;

import java.util.*;

public class AccountMigrator {
    private static final Logger LOGGER = LogManager.getLogger(AccountMigrator.class);

    private final String clientToken;
    private final Gson gson;

    public AccountMigrator(String clientToken) {
        this.clientToken = StringUtils.isBlank(clientToken) ? String.valueOf(UUID.randomUUID()) : clientToken;
        gson = new GsonBuilder()
                .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(MojangUser.class, MojangUser.getJsonizer())
                .create();
    }

    public Map<String, LegacyAccount> parse(JsonObject object) {
        Map<String, LegacyAccount> map = gson.fromJson(object, new TypeToken<Map<String, LegacyAccount>>() {
        }.getType());
        String output = gson.toJson(map);
        return map;
    }

    public List<User> migrate(Collection<LegacyAccount> unmigratedList) {
        ArrayList<User> migrated = new ArrayList<>();

        for (LegacyAccount account : unmigratedList) {
            if (account.type == null) {
                account.type = "free";
            }
            User user;
            try {
                switch (account.type) {
                    case "free":
                        user = AccountManager.getPlainAuth().authorize(account.username);
                        break;
                    case "mojang":
                        account.clientToken = clientToken;
                        user = gson.fromJson(gson.toJson(account), MojangUser.class);
                        break;
                    case "ely":
                        UUID uuid = UUIDTypeAdapter.fromString(account.uuid);
                        user = new ElyLegacyUser(account.username, uuid, account.displayName, clientToken, account.accessToken);
                        break;
                    default:
                        continue;
                }
            } catch (Exception e) {
                LOGGER.error("Could not migrate {}", account, e);
                continue;
            }
            migrated.add(user);
        }
        return migrated;
    }
}
