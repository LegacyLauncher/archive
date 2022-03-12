package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.managed.ManagedListenerHelper;
import ru.turikhay.tlauncher.user.*;

import java.util.Objects;

public final class AccountManager {
    private static final MojangAuth MOJANG_AUTH = new MojangAuth();
    private static final ElyAuth ELY_AUTH = new ElyAuth();
    private static final ElyLegacyAuth ELY_LEGACY_AUTH = new ElyLegacyAuth();
    private static final PlainAuth PLAIN_AUTH = new PlainAuth();
    private static final McleaksAuth MCLEAKS_AUTH = new McleaksAuth();
    private static final MinecraftAuth MINECRAFT_AUTH = new MinecraftAuth();

    private final ManagedListenerHelper<User> helper = new ManagedListenerHelper<>();
    private final UserSetJsonizer jsonizer = new UserSetJsonizer(helper);

    private UserSet userSet;

    public AccountManager() {
        setUserSet(null);
    }

    public UserSet getUserSet() {
        return userSet;
    }

    void setUserSet(UserSet userSet) {
        this.userSet = userSet == null ? new UserSet(helper) : userSet;
    }

    public UserSetJsonizer getTypeAdapter() {
        return jsonizer;
    }

    public void addListener(UserSetListener listener) {
        helper.addListener(listener);
    }

    public static MojangAuth getMojangAuth() {
        return MOJANG_AUTH;
    }

    public static ElyAuth getElyAuth() {
        return ELY_AUTH;
    }

    public static ElyLegacyAuth getElyLegacyAuth() {
        return ELY_LEGACY_AUTH;
    }

    public static PlainAuth getPlainAuth() {
        return PLAIN_AUTH;
    }

    public static McleaksAuth getMcleaksAuth() {
        return MCLEAKS_AUTH;
    }

    public static MinecraftAuth getMinecraftAuth() {
        return MINECRAFT_AUTH;
    }

    public static Auth<?> getAuthFor(User user) {
        Objects.requireNonNull(user, "user");
        switch (user.getType()) {
            case MojangUser.TYPE:
                return MOJANG_AUTH;
            case ElyUser.TYPE:
                return ELY_AUTH;
            case ElyLegacyUser.TYPE:
                return ELY_LEGACY_AUTH;
            case PlainUser.TYPE:
                return PLAIN_AUTH;
            case McleaksUser.TYPE:
                return MCLEAKS_AUTH;
            case MinecraftUser.TYPE:
                return MINECRAFT_AUTH;
        }
        throw new IllegalArgumentException("cannot find Auth for: " + user);
    }
}
