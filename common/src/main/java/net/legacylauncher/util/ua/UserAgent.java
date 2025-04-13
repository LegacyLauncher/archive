package net.legacylauncher.util.ua;

import java.util.List;
import java.util.Objects;

public final class UserAgent {
    public static String of(String app, String version, List<String> platform) {
        String ua = app + "/" + version;
        if (!platform.isEmpty()) {
            StringBuilder b = new StringBuilder();
            platform.stream().filter(Objects::nonNull).forEach(p -> b.append("; ").append(p));
            ua += " (" + b.substring(2) + ")";
        }
        return ua;
    }
}
