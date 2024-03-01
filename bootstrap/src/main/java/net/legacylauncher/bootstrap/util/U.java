package net.legacylauncher.bootstrap.util;

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


    private U() {
    }
}
