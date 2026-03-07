package net.legacylauncher.repository;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

public interface IRepo {
    URLConnection get(String path, int timeout) throws IOException;

    List<String> getHosts();
}
