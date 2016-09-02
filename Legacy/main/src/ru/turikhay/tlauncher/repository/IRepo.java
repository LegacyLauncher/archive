package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.Proxy;
import java.net.URLConnection;

public interface IRepo {
    URLConnection get(String path, int timeout, Proxy proxy) throws IOException;
}
