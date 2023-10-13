package net.legacylauncher.repository;

import java.io.IOException;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.List;

public interface IRepo {
    URLConnection get(String path, int timeout, Proxy proxy) throws IOException;

    List<String> getHosts();
}
