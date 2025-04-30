package net.legacylauncher.connection.bad;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BadHostsList {
    private final Set<String> BAD_HOSTS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean contains(URL url) {
        return BAD_HOSTS.contains(url.getHost());
    }

    public void add(URL url) {
        if (BAD_HOSTS.add(url.getHost())) {
            log.info("NEW BAD HOST: {}", url.getHost());
        }
    }
}
