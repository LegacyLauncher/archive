package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.Proxy;
import java.net.URLConnection;

public interface IRepo {
   URLConnection get(String var1, int var2, Proxy var3) throws IOException;
}
