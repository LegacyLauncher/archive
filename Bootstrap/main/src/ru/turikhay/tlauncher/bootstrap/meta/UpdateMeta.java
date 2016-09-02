package ru.turikhay.tlauncher.bootstrap.meta;

import java.util.Collections;
import java.util.Map;

public class UpdateMeta {
    private RemoteBootstrapMeta bootstrap;
    private RemoteLauncherMeta launcher;

    private Map<String, String> support;

    public RemoteBootstrapMeta getBootstrap() {
        return bootstrap;
    }

    public RemoteLauncherMeta getLauncher() {
        return launcher;
    }

    public Map<String, String> getSupport() {
        return support == null ? null : Collections.unmodifiableMap(support);
    }
}
