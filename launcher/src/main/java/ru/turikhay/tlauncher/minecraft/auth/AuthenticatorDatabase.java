package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AuthenticatorDatabase {
    private final List<Account<? extends User>> accounts = new ArrayList<>(), accounts_ = Collections.unmodifiableList(accounts);

    public AuthenticatorDatabase(AccountManager manager) {
        manager.addListener(set -> {
            accounts.clear();
            for (User user : set.getSet()) {
                accounts.add(new Account<>(user));
            }
        });
    }

    public Collection<Account<? extends User>> getAccounts() {
        return accounts_;
    }

    public Account<? extends User> getByUsername(String username, Account.AccountType type) {
        for (Account<? extends User> account : accounts) {
            if (account.getUsername().equals(username) && account.getType().equals(type)) {
                return account;
            }
        }
        return null;
    }
}

/*import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

public class AuthenticatorDatabase {
    private final Map<String, Account> accounts;
    private AccountListener listener;

    public AuthenticatorDatabase(Map<String, Account> map) {
        if (map == null) {
            throw new NullPointerException();
        } else {
            accounts = map;
        }
    }

    public AuthenticatorDatabase() {
        this(new LinkedHashMap());
    }

    public Collection<Account> getAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public Account getByUUID(String uuid) {
        return accounts.get(uuid);
    }

    public Account getByUsername(String username, Account.AccountType type) {
        if (username == null) {
            throw new NullPointerException();
        } else {
            Iterator var4 = accounts.values().iterator();

            Account acc;
            do {
                do {
                    if (!var4.hasNext()) {
                        return null;
                    }

                    acc = (Account) var4.next();
                } while (!username.equals(acc.getUsername()));
            } while (type != null && type != acc.getType());

            return acc;
        }
    }

    public void registerAccount(Account acc) {
        if (acc == null) {
            throw new NullPointerException();
        } else if (accounts.containsValue(acc)) {
            throw new IllegalArgumentException("This account already exists!");
        } else {
            String uuid = acc.getUUID() == null ? acc.getUsername() : acc.getUUID();
            accounts.put(uuid, acc);
            fireRefresh();
        }
    }

    public void unregisterAccount(Account acc) {
        if (acc == null) {
            throw new NullPointerException();
        } else if (!accounts.containsValue(acc)) {
            throw new IllegalArgumentException("This account doesn\'t exist!");
        } else {
            accounts.values().remove(acc);
            fireRefresh();
        }
    }

    private void fireRefresh() {
        if (listener != null) {
            listener.onAccountsRefreshed(this);
        }
    }

    public void setListener(AccountListener listener) {
        this.listener = listener;
    }

    public static class Serializer implements JsonDeserializer<AuthenticatorDatabase>, JsonSerializer<AuthenticatorDatabase> {
        public AuthenticatorDatabase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LinkedHashMap services = new LinkedHashMap();
            Map credentials = deserializeCredentials((JsonObject) json, context);
            Iterator var7 = credentials.entrySet().iterator();

            while (var7.hasNext()) {
                Entry en = (Entry) var7.next();
                services.put(en.getKey(), new Account((Map) en.getValue()));
            }

            return new AuthenticatorDatabase(services);
        }

        Map<String, Map<String, Object>> deserializeCredentials(JsonObject json, JsonDeserializationContext context) {
            LinkedHashMap result = new LinkedHashMap();
            Iterator var5 = json.entrySet().iterator();

            while (var5.hasNext()) {
                Entry authEntry = (Entry) var5.next();
                LinkedHashMap credentials = new LinkedHashMap();
                Iterator var8 = ((JsonObject) authEntry.getValue()).entrySet().iterator();

                while (var8.hasNext()) {
                    Entry credentialsEntry = (Entry) var8.next();
                    credentials.put(credentialsEntry.getKey(), deserializeCredential((JsonElement) credentialsEntry.getValue()));
                }

                result.put(authEntry.getKey(), credentials);
            }

            return result;
        }

        private Object deserializeCredential(JsonElement element) {
            Iterator var4;
            if (element instanceof JsonObject) {
                LinkedHashMap result1 = new LinkedHashMap();
                var4 = ((JsonObject) element).entrySet().iterator();

                while (var4.hasNext()) {
                    Entry entry1 = (Entry) var4.next();
                    result1.put(entry1.getKey(), deserializeCredential((JsonElement) entry1.getValue()));
                }

                return result1;
            } else if (!(element instanceof JsonArray)) {
                return element.getAsString();
            } else {
                ArrayList result = new ArrayList();
                var4 = ((JsonArray) element).iterator();

                while (var4.hasNext()) {
                    JsonElement entry = (JsonElement) var4.next();
                    result.add(deserializeCredential(entry));
                }

                return result;
            }
        }

        public JsonElement serialize(AuthenticatorDatabase src, Type typeOfSrc, JsonSerializationContext context) {
            Map services = src.accounts;
            LinkedHashMap credentials = new LinkedHashMap();
            Iterator var7 = services.entrySet().iterator();

            while (var7.hasNext()) {
                Entry en = (Entry) var7.next();
                credentials.put(en.getKey(), ((Account) en.getValue()).createMap());
            }

            return context.serialize(credentials);
        }
    }
}*/
