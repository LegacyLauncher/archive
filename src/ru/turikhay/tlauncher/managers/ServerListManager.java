package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

public class ServerListManager extends InterruptibleComponent {
   private final Gson gson;
   private final Repository repository;
   private ServerList serverList;
   private final List listeners;
   private static final String logPrefix = '[' + ServerListManager.class.getSimpleName() + ']';

   private ServerListManager(ComponentManager manager, Repository repository) throws Exception {
      super(manager);
      if (repository == null) {
         throw new NullPointerException("Repository cannot be NULL!");
      } else {
         this.repository = repository;
         this.gson = TLauncher.getGson();
         this.listeners = Collections.synchronizedList(new ArrayList());
      }
   }

   public ServerListManager(ComponentManager manager) throws Exception {
      this(manager, Repository.SERVERLIST_REPO);
   }

   public ServerList getList() {
      return this.serverList;
   }

   protected boolean refresh(int refreshID) {
      this.refreshList[refreshID] = true;
      this.log("Refreshing servers...");
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         ServerListManagerListener listener = (ServerListManagerListener)var3.next();
         listener.onServersRefreshing(this);
      }

      Object lock = new Object();
      Time.start(lock);
      ServerList result = null;
      Throwable e = null;

      try {
         result = this.loadFromList();
      } catch (Throwable var7) {
         e = var7;
      }

      if (this.isCancelled(refreshID)) {
         this.log("Server list refreshing has been cancelled (" + Time.stop(lock) + " ms)");
         return false;
      } else {
         ServerListManagerListener listener;
         Iterator var6;
         if (e != null) {
            var6 = this.listeners.iterator();

            while(var6.hasNext()) {
               listener = (ServerListManagerListener)var6.next();
               listener.onServersRefreshingFailed(this);
            }

            this.log("Cannot refresh servers (" + Time.stop(lock) + " ms)", e);
            return true;
         } else {
            if (result != null) {
               this.serverList = result;
            }

            this.log("Servers has been refreshed (" + Time.stop(lock) + " ms)");
            this.log(this.serverList);
            this.refreshList[refreshID] = false;
            var6 = this.listeners.iterator();

            while(var6.hasNext()) {
               listener = (ServerListManagerListener)var6.next();
               listener.onServersRefreshed(this);
            }

            return true;
         }
      }
   }

   public static boolean reconstructList(ServerList promoted, File serversDat) throws IOException {
      if (promoted == null) {
         throw new NullPointerException("list");
      } else if (serversDat == null) {
         throw new NullPointerException("servers.dat file");
      } else {
         slog("Reconstructing...");
         slog("Loading from file...");
         ServerList userList = ServerList.loadFromFile(serversDat);
         Iterator var4 = promoted.getList().iterator();

         while(var4.hasNext()) {
            ServerList.Server promotedServer = (ServerList.Server)var4.next();
            if (userList.contains(promotedServer)) {
               userList.remove(promotedServer);
            }
         }

         ServerList resultList = ServerList.sortLists(promoted, userList);
         resultList.save(serversDat);
         return true;
      }
   }

   private ServerList loadFromList() throws JsonSyntaxException, IOException {
      Object lock = new Object();
      Time.start(lock);
      ServerList list = (ServerList)this.gson.fromJson(this.repository.getUrl(), ServerList.class);
      this.log("Got in", Time.stop(lock), "ms");
      return list;
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }

   private static void slog(Object... o) {
      U.log(logPrefix, o);
   }
}
