package com.turikhay.tlauncher.component.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.common.CompressedStreamTools;
import net.minecraft.common.NBTTagCompound;
import net.minecraft.common.NBTTagList;

public class ServerList {
   private List list = new ArrayList();

   public void add(ServerList.Server server) {
      if (server == null) {
         throw new NullPointerException();
      } else {
         this.list.add(server);
      }
   }

   public boolean remove(ServerList.Server server) {
      if (server == null) {
         throw new NullPointerException();
      } else {
         return this.list.remove(server);
      }
   }

   public boolean contains(ServerList.Server server) {
      return this.list.contains(server);
   }

   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public List getList() {
      return Collections.unmodifiableList(this.list);
   }

   public void save(File file) throws IOException {
      NBTTagList servers = new NBTTagList();
      Iterator var4 = this.list.iterator();

      while(var4.hasNext()) {
         ServerList.Server server = (ServerList.Server)var4.next();
         servers.appendTag(server.getNBT());
      }

      NBTTagCompound compound = new NBTTagCompound();
      compound.setTag("servers", servers);
      CompressedStreamTools.safeWrite(compound, file);
   }

   public String toString() {
      return "ServerList{" + this.list.toString() + "}";
   }

   public static ServerList loadFromFile(File file) throws IOException {
      ServerList serverList = new ServerList();
      List list = serverList.list;
      NBTTagCompound compound = CompressedStreamTools.read(file);
      if (compound == null) {
         return serverList;
      } else {
         NBTTagList servers = compound.getTagList("servers");

         for(int i = 0; i < servers.tagCount(); ++i) {
            list.add(ServerList.Server.loadFromNBT((NBTTagCompound)servers.tagAt(i)));
         }

         return serverList;
      }
   }

   public static ServerList sortLists(ServerList pref, ServerList add) {
      ServerList serverList = new ServerList();
      List list = serverList.list;
      List prefList = pref.list;
      List addList = add.list;
      serverList.list.addAll(prefList);
      Iterator var7 = addList.iterator();

      while(var7.hasNext()) {
         ServerList.Server server = (ServerList.Server)var7.next();
         if (!list.contains(server)) {
            list.add(server);
         }
      }

      return serverList;
   }

   public static class Server {
      private String name;
      private String version;
      private String address;
      private boolean hideAddress;
      private int acceptTextures;

      public String getName() {
         return this.name;
      }

      public String getVersion() {
         return this.version;
      }

      public String getAddress() {
         return this.address;
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else if (!(obj instanceof ServerList.Server)) {
            return false;
         } else {
            ServerList.Server server = (ServerList.Server)obj;
            return server.address.equals(this.address);
         }
      }

      public String toString() {
         return "{'" + this.name + "', '" + this.address + "', '" + this.version + "'}";
      }

      public static ServerList.Server loadFromNBT(NBTTagCompound nbt) {
         ServerList.Server server = new ServerList.Server();
         server.name = nbt.getString("name");
         server.address = nbt.getString("ip");
         server.hideAddress = nbt.getBoolean("hideAddress");
         if (nbt.hasKey("acceptTextures")) {
            server.acceptTextures = nbt.getBoolean("acceptTextures") ? 1 : -1;
         }

         return server;
      }

      public NBTTagCompound getNBT() {
         NBTTagCompound compound = new NBTTagCompound();
         compound.setString("name", this.name);
         compound.setString("ip", this.address);
         compound.setBoolean("hideAddress", this.hideAddress);
         if (this.acceptTextures != 0) {
            compound.setBoolean("acceptTextures", this.acceptTextures == 1);
         }

         return compound;
      }
   }
}
