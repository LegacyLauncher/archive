package ru.turikhay.tlauncher.portals;

import ru.turikhay.util.JavaVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Portals {
    private Portals() {
    }

    private static final Portal PORTAL;

    public static Portal getPortal() {
        return PORTAL;
    }

    private static boolean isLinux() {
        String os = System.getProperty("os.name");
        if (os == null) return false;
        return os.toLowerCase(Locale.ROOT).contains("linux");
    }

    static {
        List<Portal> portals = new ArrayList<>();
        if (JavaVersion.getCurrent().getMajor() >= 11 && isLinux()) {
            try {
                XDGPortal.tryToCreate().ifPresent(portals::add);
            } catch (NoClassDefFoundError ignored) {
                // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
                // => older bootstrap version
            }
        }
        JVMPortal.tryToCreate().ifPresent(portals::add);
        switch (portals.size()) {
            case 0:
                PORTAL = new NoopPortal();
                break;
            case 1:
                PORTAL = portals.get(0);
                break;
            default:
                PORTAL = new PortalCombiner(portals);
                break;
        }
    }
}
