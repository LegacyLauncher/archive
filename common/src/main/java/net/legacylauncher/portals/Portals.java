package net.legacylauncher.portals;

import java.util.Optional;

public class Portals {
    private Portals() {
    }

    private static final Portal PORTAL;

    public static Portal getPortal() {
        return PORTAL;
    }


    static {
        Optional<JVMPortal> jvmPortal = JVMPortal.tryToCreate();
        if (jvmPortal.isPresent()) {
            PORTAL = jvmPortal.get();
        } else {
            PORTAL = new NoopPortal();
        }
    }
}
