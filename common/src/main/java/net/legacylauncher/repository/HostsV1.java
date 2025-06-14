package net.legacylauncher.repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HostsV1 {
    public static final List<String> BOOTSTRAP = listOf(
            "bootstrap.llaun.ch",
            "bootstrap.legacylauncher.ru"
    );

    public static final List<String> PROXY = listOf(
            "proxy.llaun.ch",
            "proxy.legacylauncher.ru"
    );

    public static final List<String> REPO = listOf(
            "repo.llaun.ch",
            "repo.legacylauncher.ru"
    );

    private static List<String> listOf(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }
}
