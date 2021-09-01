package ru.turikhay.tlauncher.user;

import com.mojang.authlib.exceptions.AuthenticationException;

public class MojangAccountMigratedException extends AuthException {
    MojangAccountMigratedException() {
        super("This Mojang account is already migrated to Microsoft", "mojang-migrated");
    }

    public static boolean detect(AuthenticationException e) {
        return ("Gone (410) - The requested resource is no longer available at the server and" +
                " no forwarding address is known").equals(e.getMessage());
    }
}
