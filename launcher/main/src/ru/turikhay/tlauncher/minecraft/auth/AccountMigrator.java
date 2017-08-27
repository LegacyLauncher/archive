package ru.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.user.ElyLegacyUser;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.MojangUserJsonizer;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.util.*;

public class AccountMigrator {
    private final String clientToken;
    private final Gson gson;

    public AccountMigrator(String clientToken) {
        this.clientToken = StringUtil.requireNotBlank(clientToken);
        gson = new GsonBuilder()
                .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(MojangUser.class, MojangUser.getJsonizer())
                .create();
    }

    public Map<String, LegacyAccount> parse(JsonObject object) {
        return gson.fromJson(object, new TypeToken<Map<String, LegacyAccount>>(){}.getType());
    }

    public List<User> migrate(Collection<LegacyAccount> unmigratedList) {
        ArrayList<User> migrated = new ArrayList<>();

        for(LegacyAccount account : unmigratedList) {
            if(account.type == null) {
                account.type = "free";
            }
            User user;
            switch(account.type) {
                case "free":
                    user = AccountManager.getPlainAuth().authorize(account.username);
                    break;
                case "mojang":
                    account.clientToken = clientToken;
                    user = gson.fromJson(gson.toJson(account), MojangUser.class);
                    break;
                case "ely":
                    user = new ElyLegacyUser(account.username, account.uuid, account.displayName, account.accessToken, clientToken);
                    break;
                default:
                    continue;
            }
            migrated.add(user);
        }
        return migrated;
    }

    private void log(Object...o) {
        U.log("[AccountMigrator]", o);
    }
}
