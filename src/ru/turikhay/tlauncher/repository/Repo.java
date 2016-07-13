package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import ru.turikhay.util.U;

public abstract class Repo implements IRepo {
   private final String name;
   private final String logPrefix;

   public Repo(String name) {
      this.name = (String)U.requireNotNull(name);
      this.logPrefix = '[' + name + ']';
   }

   public String toString() {
      return this.name;
   }

   public final URLConnection get(String path, int timeout, Proxy proxy) throws IOException {
      URL url = (URL)U.requireNotNull(this.makeUrl(path), "url");
      URLConnection connection = (URLConnection)U.requireNotNull(this.makeConnection(url, timeout, proxy), "connection");
      return connection;
   }

   protected URLConnection makeConnection(URL url, int timeout, Proxy proxy) throws IOException {
      URLConnection connection = url.openConnection(proxy);
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setUseCaches(false);
      connection.setDefaultUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
      connection.setRequestProperty("Pragma", "no-cache");
      connection.setRequestProperty("Expires", "0");
      return connection;
   }

   protected abstract URL makeUrl(String var1) throws IOException;
}
