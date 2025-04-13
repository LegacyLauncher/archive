package net.legacylauncher.bootstrap.util;

import net.legacylauncher.bootstrap.BuildConfig;
import net.legacylauncher.util.ua.UserAgent;

import java.net.URLConnection;
import java.util.Arrays;

public class BootstrapUserAgent {
    public static String USER_AGENT = UserAgent.of(
            "LL-Bootstrap",
            BuildConfig.VERSION,
            Arrays.asList(
                    System.getProperty("os.name"),
                    System.getProperty("os.name") + " " + System.getProperty("os.version"),
                    System.getProperty("os.arch"),
                    "Java/" + System.getProperty("java.version")
            )
    );

    public static void set(URLConnection c) {
        c.setRequestProperty("User-Agent", USER_AGENT);
    }
}
