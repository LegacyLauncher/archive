package com.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.InterruptibleComponent;
import com.turikhay.tlauncher.repository.Repository;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ServerListManager extends InterruptibleComponent {
   private final Gson gson;
   private final Repository repository;
   private ServerList serverList;
   private final List listeners;

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

   public boolean reconstructList(String version, File listFile) throws IOException {
      this.log("Reconstructing server list (servers.dat)...");
      if (version == null) {
         throw new NullPointerException("Version cannot be NULL!");
      } else if (listFile == null) {
         throw new NullPointerException("File cannot be NULL!");
      } else if (this.serverList == null) {
         this.log("Promoted server list is NULL. Server list won't be reconstructed.");
         return false;
      } else if (this.serverList.isEmpty()) {
         this.log("Promoted server list is empty. Server list won't be reconstructed.");
         return false;
      } else {
         ServerList list = new ServerList();
         Iterator var5 = this.serverList.getList().iterator();

         while(var5.hasNext()) {
            ServerList.Server prefServer = (ServerList.Server)var5.next();
            if (version.matches(prefServer.getVersion())) {
               list.add(prefServer);
            }
         }

         ServerList userList = ServerList.loadFromFile(listFile);
         Iterator var6 = this.serverList.getList().iterator();

         while(var6.hasNext()) {
            ServerList.Server prefServer = (ServerList.Server)var6.next();
            if (userList.contains(prefServer)) {
               userList.remove(prefServer);
            }
         }

         ServerList resultList = ServerList.sortLists(list, userList);
         resultList.save(listFile);
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
}
