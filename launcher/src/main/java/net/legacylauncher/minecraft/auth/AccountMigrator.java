package net.legacylauncher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.properties.PropertyMap;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.managers.AccountManager;
import net.legacylauncher.user.ElyLegacyUser;
import net.legacylauncher.user.MojangUser;
import net.legacylauncher.user.User;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class AccountMigrator {
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
                        user = AccountManager.getPlainAuth().authorize(account.username, true);
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
                log.error("Could not migrate {}", account, e);
                continue;
            }
            migrated.add(user);
        }
        return migrated;
    }
}
