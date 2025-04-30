package net.legacylauncher.bootstrap.util;

import net.legacylauncher.connection.bad.BadHostsList;

import java.net.Proxy;
import java.util.Locale;

public final class U {
    public static Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public static Locale getLocale(String locale) {
        if (locale == null) {
            return null;
        }
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.toString().equals(locale)) {
                return l;
            }
        }
        return null;
    }

    public static final BadHostsList BAD_HOSTS = new BadHostsList();

    private U() {
    }
}
