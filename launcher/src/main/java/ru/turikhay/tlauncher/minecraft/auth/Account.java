package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.User;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class Account<T extends User> {
    protected final T user;

    private Account() {
        this.user = null;
    }

    public Account(T user) {
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account<?> account = (Account<?>) o;

        return user != null && user.equals(account.user);
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : super.hashCode();
    }

    public T getUser() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }

    public AccountType getType() {
        AccountType type = AccountType.parse(user.getType());
        if (type == null) throw new IllegalArgumentException("No account type was found for value " + user.getType());
        return type;
    }

    public boolean isFree() {
        return getType().equals(AccountType.PLAIN);
    }

    public static Account<?> randomAccount() {
        return new Account<>();
    }

    @Override
    public String toString() {
        return "Account{" +
                "user=" + user +
                '}';
    }

    public enum AccountType {
        ELY("logo-ely"),
        ELY_LEGACY("logo-ely"),
        MOJANG("logo-mojang"),
        MINECRAFT("logo-microsoft"),
        MCLEAKS("logo-mcleaks"),
        PLAIN(null);

        private final String icon;

        AccountType(String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }

        public String toString() {
            return super.toString().toLowerCase(java.util.Locale.ROOT);
        }

        private static final Collection<AccountType> VALUES = Arrays.asList(values());

        @Nullable
        public static AccountType parse(String name) {
            return VALUES.stream().filter(e -> e.name().equalsIgnoreCase(name)).findAny().orElse(null);
        }
    }
}
/*public class Account {
    private String username;
    private String userID;
    private String displayName;
    private String password;
    private String accessToken;
    private String uuid;
    private List<Map<String, String>> userProperties;
    private Account.AccountType type;
    private GameProfile[] profiles;
    private GameProfile selectedProfile;
    private User user;

    public Account() {
        type = Account.AccountType.PLAIN;
    }

    public Account(String username) {
        this();
        setUsername(username);
    }

    public Account(Map<String, Object> map) {
        this();
        fillFromMap(map);
    }

    public String getUsername() {
        return username;
    }

    public boolean hasUsername() {
        return username != null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDisplayName() {
        return displayName == null ? username : displayName;
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public void setPassword(char[] password) {
        setPassword(new String(password));
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        if ("null".equals(accessToken)) {
            accessToken = null;
        }

        this.accessToken = accessToken;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public GameProfile[] getProfiles() {
        return profiles;
    }

    public void setProfiles(GameProfile[] p) {
        profiles = p;
    }

    public GameProfile getProfile() {
        return selectedProfile != null ? selectedProfile : GameProfile.DEFAULT_PROFILE;
    }

    public void setProfile(GameProfile p) {
        selectedProfile = p;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, List<String>> getProperties() {
        HashMap map = new HashMap();
        ArrayList list = new ArrayList();
        Map property;
        Iterator var4;
        if (userProperties != null) {
            var4 = userProperties.iterator();

            while (var4.hasNext()) {
                property = (Map) var4.next();
                list.add(new UserProperty((String) property.get("name"), (String) property.get("value")));
            }
        }

        if (user != null && user.getProperties() != null) {
            var4 = user.getProperties().iterator();

            while (var4.hasNext()) {
                property = (Map) var4.next();
                list.add(new UserProperty((String) property.get("name"), (String) property.get("value")));
            }
        }

        var4 = list.iterator();

        while (var4.hasNext()) {
            UserProperty property1 = (UserProperty) var4.next();
            ArrayList values = new ArrayList();
            values.add(property1.getValue());
            map.put(property1.getKey(), values);
        }

        return map;
    }

    void setProperties(List<Map<String, String>> properties) {
        userProperties = properties;
    }

    public Account.AccountType getType() {
        return type;
    }

    public void setType(Account.AccountType type) {
        if (type == null) {
            throw new NullPointerException();
        } else {
            this.type = type;
        }
    }

    public boolean isFree() {
        return type.equals(Account.AccountType.PLAIN);
    }

    Map<String, Object> createMap() {
        HashMap r = new HashMap();
        r.put("username", username);
        r.put("userid", userID);
        r.put("uuid", UUIDTypeAdapter.toUUID(uuid));
        r.put("displayName", displayName);
        if (!isFree()) {
            r.put("type", type);
            r.put("accessToken", accessToken);
        }

        if (userProperties != null) {
            r.put("userProperties", userProperties);
        }

        return r;
    }

    void fillFromMap(Map<String, Object> map) {
        if (map.containsKey("username")) {
            setUsername(map.get("username").toString());
        }

        setUserID(map.containsKey("userid") ? map.get("userid").toString() : getUsername());
        setDisplayName(map.containsKey("displayName") ? map.get("displayName").toString() : getUsername());
        setProperties(map.containsKey("userProperties") ? (List) map.get("userProperties") : null);
        setUUID(map.containsKey("uuid") ? UUIDTypeAdapter.toUUID(map.get("uuid").toString()) : null);
        boolean hasAccessToken = map.containsKey("accessToken");
        if (hasAccessToken) {
            setAccessToken(map.get("accessToken").toString());
        }

        setType(map.containsKey("type") ? Reflect.parseEnum(AccountType.class, map.get("type").toString()) : (hasAccessToken ? Account.AccountType.MOJANG : Account.AccountType.PLAIN));
    }

    public void complete(Account acc) {
        if (acc == null) {
            throw new NullPointerException();
        } else {
            boolean sameName = acc.username.equals(username);
            username = acc.username;
            type = acc.type;
            if (acc.userID != null) {
                userID = acc.userID;
            }

            if (acc.displayName != null) {
                displayName = acc.displayName;
            }

            if (acc.password != null) {
                password = acc.password;
            }

            if (!sameName) {
                accessToken = null;
            }

        }
    }

    public boolean equals(Account acc) {
        return acc == null ? false : (username == null ? acc.username == null : username.equals(acc.username) && type.equals(acc.type) && (password == null || password.equals(acc.password)));
    }

    public String toString() {
        return toDebugString();
    }

    public String toDebugString() {
        Map map = createMap();
        if (map.containsKey("userProperties")) {
            map.remove("userProperties");
            map.put("userProperties", "(not null)");
        }
        if (map.containsKey("accessToken")) {
            map.remove("accessToken");
            map.put("accessToken", "(not null)");
        }

        return "Account" + map;
    }

    public static Account randomAccount() {
        return new Account("random" + (new Random()).nextLong());
    }


}*/
